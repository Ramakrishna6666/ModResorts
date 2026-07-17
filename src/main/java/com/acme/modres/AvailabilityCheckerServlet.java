package com.acme.modres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acme.modres.mbean.IOUtils;
import com.acme.modres.mbean.reservation.DateChecker;
import com.acme.modres.mbean.reservation.ReservationCheckerData;
import com.acme.modres.mbean.reservation.Reservation;

import com.acme.modres.util.ZipValidator;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@WebServlet({ "/resorts/availability" })
public class AvailabilityCheckerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(AvailabilityCheckerServlet.class.getName());

  private static InitialContext context;

  private ReservationCheckerData reservationCheckerData;

  // S3 configuration from environment variables
  private static final String S3_BUCKET_NAME = System.getenv("S3_BUCKET_NAME") != null
      ? System.getenv("S3_BUCKET_NAME") : "modresorts-data";

  @Override
  public void init() {
    // load reserved dates
    this.reservationCheckerData = new ReservationCheckerData(IOUtils.getReservationListFromConfig());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

    String methodName = "doGet";
    logger.entering(AvailabilityCheckerServlet.class.getName(), methodName);
    int statusCode = 200;

    String selectedDateStr = request.getParameter("date");
    boolean parsedDate = reservationCheckerData.setSelectedDate(selectedDateStr);
    if (!parsedDate || reservationCheckerData.getReservationList() == null) {
      statusCode = 500;
      reservationCheckerData.setAvailablility(false);
    } else {
      List<Reservation> reservations = reservationCheckerData.getReservationList().getReservations();
      boolean isAvailible = true;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATA_FORMAT);

      for (Reservation reservation : reservations) {
        try {
          // blocker-10, blocker-11: Replace java.util.Date with java.time API (LocalDate/UTC)
          LocalDate fromDate = LocalDate.parse(reservation.getFromDate(), formatter);
          LocalDate toDate = LocalDate.parse(reservation.getToDate(), formatter);
          LocalDate selectedDate = reservationCheckerData.getSelectedDate();

          if (selectedDate.isAfter(fromDate) && selectedDate.isBefore(toDate)) {
            isAvailible = false;
            break;
          }
        } catch (DateTimeParseException ex) {
          ex.printStackTrace();
        }
      }

      reservationCheckerData.setAvailablility(isAvailible);

      // Adjust the status code based on availability
      if (!isAvailible) {
        statusCode = 201;
      }
    }

    // Send the response
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    out.print("{\"availability\": \"" + String.valueOf(reservationCheckerData.isAvailible()) + "\"}");
    response.setStatus(statusCode);
  }

  /**
   * Returns the weather information for a given city
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    doGet(request, response);
  }

  /**
   * blocker-1 (Hard-coded File Paths), blocker-2 (Local File System Write Operations),
   * blocker-4 (java.io.File Usage), blocker-5 (Resource Leaks):
   * Replaced hard-coded file paths and local file write operations with Amazon S3.
   * Uses try-with-resources for automatic resource management.
   * Writes the reservations zip directly to S3 instead of local file system.
   */
  protected int exportRevervations(String selectedDateStr) {
    String s3Key = "exports/reservations.zip";

    // blocker-5: Use try-with-resources for automatic resource management
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zipOut = new ZipOutputStream(baos)) {

      // blocker-1, blocker-4: Read reservations.json from S3 instead of hard-coded file path
      S3Client s3Client = S3Client.builder().build();
      byte[] fileBytes;
      try (software.amazon.awssdk.core.ResponseInputStream<software.amazon.awssdk.services.s3.model.GetObjectResponse> s3Object =
               s3Client.getObject(GetObjectRequest.builder()
                   .bucket(S3_BUCKET_NAME)
                   .key("data/reservations.json")
                   .build())) {
        fileBytes = s3Object.readAllBytes();
      }

      ZipEntry zipEntry = new ZipEntry("reservations.json");
      zipOut.putNextEntry(zipEntry);
      zipOut.write(fileBytes);
      zipOut.closeEntry();
      zipOut.finish();

      byte[] zipBytes = baos.toByteArray();

      // blocker-2: Write zip to Amazon S3 instead of local file system
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(S3_BUCKET_NAME)
              .key(s3Key)
              .contentType("application/zip")
              .build(),
          RequestBody.fromBytes(zipBytes));

      logger.info("Reservations exported to S3: s3://" + S3_BUCKET_NAME + "/" + s3Key);
      return 0;

    } catch (IOException e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return -1;
  }

}
