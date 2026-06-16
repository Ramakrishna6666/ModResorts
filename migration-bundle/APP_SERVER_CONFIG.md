# Application Server Configuration Examples

This document provides configuration examples for setting up PostgreSQL DataSource in various Jakarta EE application servers.

## Table of Contents
1. [WildFly / JBoss EAP](#wildfly--jboss-eap)
2. [Apache Tomcat](#apache-tomcat)
3. [Payara / GlassFish](#payara--glassfish)
4. [IBM WebSphere Liberty](#ibm-websphere-liberty)
5. [Oracle WebLogic](#oracle-weblogic)

---

## WildFly / JBoss EAP

### 1. Add PostgreSQL Module

Create directory structure:
```bash
mkdir -p $WILDFLY_HOME/modules/org/postgresql/main
```

Create `module.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.9" name="org.postgresql">
    <resources>
        <resource-root path="postgresql-42.7.1.jar"/>
    </resources>
    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
```

Copy PostgreSQL driver:
```bash
cp postgresql-42.7.1.jar $WILDFLY_HOME/modules/org/postgresql/main/
```

### 2. Configure DataSource

Edit `standalone.xml` or `domain.xml`:

```xml
<subsystem xmlns="urn:jboss:domain:datasources:6.0">
    <datasources>
        <!-- PostgreSQL DataSource -->
        <datasource jndi-name="java:/jdbc/ModResortsJndi" 
                    pool-name="ModResortsPool" 
                    enabled="true" 
                    use-java-context="true">
            <connection-url>jdbc:postgresql://localhost:5432/modresorts</connection-url>
            <driver>postgresql</driver>
            <security>
                <user-name>modresorts_user</user-name>
                <password>modresorts_pass</password>
            </security>
            <validation>
                <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
                <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"/>
            </validation>
            <pool>
                <min-pool-size>5</min-pool-size>
                <max-pool-size>20</max-pool-size>
                <prefill>true</prefill>
            </pool>
            <timeout>
                <idle-timeout-minutes>15</idle-timeout-minutes>
            </timeout>
            <statement>
                <prepared-statement-cache-size>32</prepared-statement-cache-size>
                <share-prepared-statements>true</share-prepared-statements>
            </statement>
        </datasource>
        
        <drivers>
            <driver name="postgresql" module="org.postgresql">
                <driver-class>org.postgresql.Driver</driver-class>
                <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
            </driver>
        </drivers>
    </datasources>
</subsystem>
```

### 3. Test DataSource

```bash
# Using CLI
$WILDFLY_HOME/bin/jboss-cli.sh --connect
/subsystem=datasources/data-source=ModResortsPool:test-connection-in-pool
```

---

## Apache Tomcat

### 1. Add PostgreSQL Driver

Copy driver to Tomcat lib:
```bash
cp postgresql-42.7.1.jar $CATALINA_HOME/lib/
```

### 2. Configure DataSource in context.xml

**Option A: Global Configuration** (`$CATALINA_HOME/conf/context.xml`):
```xml
<Context>
    <Resource name="jdbc/ModResortsJndi"
              auth="Container"
              type="javax.sql.DataSource"
              driverClassName="org.postgresql.Driver"
              url="jdbc:postgresql://localhost:5432/modresorts"
              username="modresorts_user"
              password="modresorts_pass"
              maxTotal="20"
              maxIdle="10"
              minIdle="5"
              maxWaitMillis="10000"
              testOnBorrow="true"
              testWhileIdle="true"
              timeBetweenEvictionRunsMillis="30000"
              minEvictableIdleTimeMillis="60000"
              validationQuery="SELECT 1"
              validationQueryTimeout="5"/>
</Context>
```

**Option B: Application-Specific** (`META-INF/context.xml` in WAR):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Resource name="jdbc/ModResortsJndi"
              auth="Container"
              type="javax.sql.DataSource"
              factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
              driverClassName="org.postgresql.Driver"
              url="jdbc:postgresql://localhost:5432/modresorts"
              username="modresorts_user"
              password="modresorts_pass"
              
              <!-- Connection Pool Settings -->
              initialSize="5"
              maxActive="20"
              maxIdle="10"
              minIdle="5"
              maxWait="10000"
              
              <!-- Connection Validation -->
              testOnBorrow="true"
              testOnReturn="false"
              testWhileIdle="true"
              validationQuery="SELECT 1"
              validationInterval="30000"
              
              <!-- Connection Eviction -->
              timeBetweenEvictionRunsMillis="30000"
              minEvictableIdleTimeMillis="60000"
              
              <!-- Prepared Statement Cache -->
              maxOpenPreparedStatements="100"
              
              <!-- Connection Properties -->
              connectionProperties="ApplicationName=ModResorts;reWriteBatchedInserts=true"
              
              <!-- Leak Detection -->
              removeAbandoned="true"
              removeAbandonedTimeout="60"
              logAbandoned="true"
              
              <!-- JMX -->
              jmxEnabled="true"/>
</Context>
```

### 3. Configure web.xml

Add resource reference in `WEB-INF/web.xml`:
```xml
<web-app>
    <resource-ref>
        <description>PostgreSQL DataSource</description>
        <res-ref-name>jdbc/ModResortsJndi</res-ref-name>
        <res-type>javax.sql.DataSource</res-type>
        <res-auth>Container</res-auth>
    </resource-ref>
</web-app>
```

---

## Payara / GlassFish

### 1. Add PostgreSQL Driver

Copy driver to domain lib:
```bash
cp postgresql-42.7.1.jar $PAYARA_HOME/glassfish/domains/domain1/lib/
```

Or use Admin Console: Resources → JDBC → JDBC Drivers → Add

### 2. Create Connection Pool

**Using Admin Console:**
1. Resources → JDBC → JDBC Connection Pools → New
2. Pool Name: `ModResortsPool`
3. Resource Type: `javax.sql.DataSource`
4. Database Driver Vendor: `PostgreSQL`

**Using asadmin CLI:**
```bash
asadmin create-jdbc-connection-pool \
    --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
    --restype javax.sql.DataSource \
    --property user=modresorts_user:password=modresorts_pass:serverName=localhost:portNumber=5432:databaseName=modresorts \
    ModResortsPool

# Set pool properties
asadmin set resources.jdbc-connection-pool.ModResortsPool.steady-pool-size=5
asadmin set resources.jdbc-connection-pool.ModResortsPool.max-pool-size=20
asadmin set resources.jdbc-connection-pool.ModResortsPool.pool-resize-quantity=2
asadmin set resources.jdbc-connection-pool.ModResortsPool.idle-timeout-in-seconds=300
asadmin set resources.jdbc-connection-pool.ModResortsPool.max-wait-time-in-millis=60000
asadmin set resources.jdbc-connection-pool.ModResortsPool.is-connection-validation-required=true
asadmin set resources.jdbc-connection-pool.ModResortsPool.connection-validation-method=custom-validation
asadmin set resources.jdbc-connection-pool.ModResortsPool.validation-classname=org.glassfish.api.jdbc.validation.PostgresConnectionValidation
```

### 3. Create JDBC Resource

**Using Admin Console:**
1. Resources → JDBC → JDBC Resources → New
2. JNDI Name: `jdbc/ModResortsJndi`
3. Pool Name: `ModResortsPool`

**Using asadmin CLI:**
```bash
asadmin create-jdbc-resource \
    --connectionpoolid ModResortsPool \
    jdbc/ModResortsJndi
```

### 4. Test Connection

```bash
asadmin ping-connection-pool ModResortsPool
```

---

## IBM WebSphere Liberty

### 1. Add PostgreSQL Driver

Copy driver to server directory:
```bash
cp postgresql-42.7.1.jar $WLP_HOME/usr/servers/defaultServer/lib/
```

### 2. Configure server.xml

Edit `$WLP_HOME/usr/servers/defaultServer/server.xml`:

```xml
<server description="ModResorts Server">
    <!-- Enable JDBC features -->
    <featureManager>
        <feature>jdbc-4.3</feature>
        <feature>jndi-1.0</feature>
    </featureManager>

    <!-- PostgreSQL Library -->
    <library id="PostgreSQLLib">
        <fileset dir="${server.config.dir}/lib" includes="postgresql-42.7.1.jar"/>
    </library>

    <!-- DataSource Configuration -->
    <dataSource id="ModResortsDataSource" 
                jndiName="jdbc/ModResortsJndi"
                type="javax.sql.DataSource">
        <jdbcDriver libraryRef="PostgreSQLLib"/>
        
        <properties.postgresql 
            serverName="localhost"
            portNumber="5432"
            databaseName="modresorts"
            user="modresorts_user"
            password="modresorts_pass"
            currentSchema="public"
            applicationName="ModResorts"
            ssl="false"/>
        
        <connectionManager 
            maxPoolSize="20"
            minPoolSize="5"
            connectionTimeout="30s"
            maxIdleTime="15m"
            reapTime="3m"
            agedTimeout="30m"
            purgePolicy="EntirePool"/>
    </dataSource>
</server>
```

### 3. Configure Variable Substitution (Optional)

For environment-specific configuration:

```xml
<server>
    <variable name="db.host" defaultValue="localhost"/>
    <variable name="db.port" defaultValue="5432"/>
    <variable name="db.name" defaultValue="modresorts"/>
    <variable name="db.user" defaultValue="modresorts_user"/>
    <variable name="db.password" defaultValue="modresorts_pass"/>

    <dataSource id="ModResortsDataSource" jndiName="jdbc/ModResortsJndi">
        <jdbcDriver libraryRef="PostgreSQLLib"/>
        <properties.postgresql 
            serverName="${db.host}"
            portNumber="${db.port}"
            databaseName="${db.name}"
            user="${db.user}"
            password="${db.password}"/>
    </dataSource>
</server>
```

---

## Oracle WebLogic

### 1. Add PostgreSQL Driver

**Using Admin Console:**
1. Domain Structure → Services → Data Sources
2. Lock & Edit
3. New → Generic Data Source

**Or copy manually:**
```bash
cp postgresql-42.7.1.jar $DOMAIN_HOME/lib/
```

### 2. Create Data Source

**Using Admin Console:**

1. **Configuration Tab:**
   - Name: `ModResortsDataSource`
   - JNDI Name: `jdbc/ModResortsJndi`
   - Database Type: `PostgreSQL`
   - Database Driver: `PostgreSQL's Driver (Type 4) Versions: Any`

2. **Connection Properties:**
   - Database Name: `modresorts`
   - Host Name: `localhost`
   - Port: `5432`
   - Database User Name: `modresorts_user`
   - Password: `modresorts_pass`

3. **Test Configuration:**
   - Click "Test Configuration"
   - Should see "Connection test succeeded"

4. **Target:**
   - Select your managed server or cluster
   - Click "Finish"

**Using WLST Script:**

```python
# connect to admin server
connect('weblogic','password','t3://localhost:7001')

# start edit session
edit()
startEdit()

# create data source
cd('/')
cmo.createJDBCSystemResource('ModResortsDataSource')

cd('/JDBCSystemResources/ModResortsDataSource/JDBCResource/ModResortsDataSource')
cmo.setName('ModResortsDataSource')

cd('/JDBCSystemResources/ModResortsDataSource/JDBCResource/ModResortsDataSource/JDBCDataSourceParams/ModResortsDataSource')
set('JNDINames',jarray.array([String('jdbc/ModResortsJndi')], String))

cd('/JDBCSystemResources/ModResortsDataSource/JDBCResource/ModResortsDataSource/JDBCDriverParams/ModResortsDataSource')
cmo.setUrl('jdbc:postgresql://localhost:5432/modresorts')
cmo.setDriverName('org.postgresql.Driver')
cmo.setPassword('modresorts_pass')

cd('/JDBCSystemResources/ModResortsDataSource/JDBCResource/ModResortsDataSource/JDBCDriverParams/ModResortsDataSource/Properties/ModResortsDataSource')
cmo.createProperty('user')

cd('/JDBCSystemResources/ModResortsDataSource/JDBCResource/ModResortsDataSource/JDBCDriverParams/ModResortsDataSource/Properties/ModResortsDataSource/Properties/user')
cmo.setValue('modresorts_user')

cd('/JDBCSystemResources/ModResortsDataSource/JDBCResource/ModResortsDataSource/JDBCConnectionPoolParams/ModResortsDataSource')
cmo.setInitialCapacity(5)
cmo.setMaxCapacity(20)
cmo.setMinCapacity(5)
cmo.setTestConnectionsOnReserve(true)
cmo.setTestTableName('SQL SELECT 1')

cd('/JDBCSystemResources/ModResortsDataSource')
set('Targets',jarray.array([ObjectName('com.bea:Name=AdminServer,Type=Server')], ObjectName))

# save and activate
save()
activate()
```

---

## Environment-Specific Configuration

### Development
```properties
db.url=jdbc:postgresql://localhost:5432/modresorts_dev
db.username=dev_user
db.password=dev_pass
db.pool.maximumPoolSize=5
```

### Testing
```properties
db.url=jdbc:postgresql://test-db-server:5432/modresorts_test
db.username=test_user
db.password=test_pass
db.pool.maximumPoolSize=10
```

### Production
```properties
db.url=jdbc:postgresql://prod-db-server:5432/modresorts_prod
db.username=prod_user
db.password=secure_prod_password
db.pool.maximumPoolSize=50
db.postgresql.ssl=true
db.postgresql.sslmode=require
```

---

## Verification

### Test JNDI Lookup

```java
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

public class DataSourceTest {
    public static void main(String[] args) {
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup("jdbc/ModResortsJndi");
            
            try (Connection conn = ds.getConnection()) {
                System.out.println("Connection successful!");
                System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("Version: " + conn.getMetaData().getDatabaseProductVersion());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## Troubleshooting

### ClassNotFoundException: org.postgresql.Driver
- Ensure PostgreSQL driver is in correct location
- Restart application server
- Check server logs for deployment errors

### JNDI Name Not Found
- Verify JNDI name matches in configuration
- Check DataSource is deployed/enabled
- Verify application has resource-ref in web.xml

### Connection Timeout
- Check database is running and accessible
- Verify firewall rules
- Check connection pool settings
- Increase timeout values

### Too Many Connections
- Reduce max pool size
- Check for connection leaks
- Increase PostgreSQL max_connections
- Implement connection monitoring

---

## Best Practices

1. **Use Connection Pooling**: Always configure connection pools
2. **Set Appropriate Pool Sizes**: Based on application load
3. **Enable Connection Validation**: Test connections before use
4. **Configure Timeouts**: Prevent hanging connections
5. **Use SSL in Production**: Encrypt database connections
6. **Monitor Connections**: Track pool usage and performance
7. **Externalize Configuration**: Use environment variables
8. **Test Thoroughly**: Verify configuration in each environment
9. **Document Changes**: Keep configuration documented
10. **Regular Maintenance**: Monitor and tune as needed
