#!/bin/bash

# PostgreSQL Migration Verification Script
# This script verifies that the PostgreSQL migration was successful

echo "=========================================="
echo "PostgreSQL Migration Verification"
echo "=========================================="
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check counter
CHECKS_PASSED=0
CHECKS_FAILED=0

# Function to check if file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} Found: $1"
        ((CHECKS_PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} Missing: $1"
        ((CHECKS_FAILED++))
        return 1
    fi
}

# Function to check if string exists in file
check_content() {
    if grep -q "$2" "$1" 2>/dev/null; then
        echo -e "${GREEN}✓${NC} Verified: $3"
        ((CHECKS_PASSED++))
        return 0
    else
        echo -e "${RED}✗${NC} Not found: $3"
        ((CHECKS_FAILED++))
        return 1
    fi
}

echo "1. Checking PostgreSQL Dependencies..."
echo "--------------------------------------"
check_file "pom.xml"
check_content "pom.xml" "org.postgresql" "PostgreSQL JDBC driver in pom.xml"
check_content "pom.xml" "postgresql" "PostgreSQL artifact in pom.xml"
echo ""

echo "2. Checking Configuration Files..."
echo "--------------------------------------"
check_file "src/main/resources/database.properties"
check_content "src/main/resources/database.properties" "jdbc:postgresql" "PostgreSQL JDBC URL"
check_content "src/main/resources/database.properties" "org.postgresql.Driver" "PostgreSQL driver class"
echo ""

echo "3. Checking Java Source Files..."
echo "--------------------------------------"
check_file "src/main/java/com/acme/modres/db/PostgreSQLDataSourceConfig.java"
check_file "src/main/java/com/acme/modres/db/ModResortsCustomerInformation.java"
check_content "src/main/java/com/acme/modres/db/ModResortsCustomerInformation.java" "public.customer" "PostgreSQL schema qualification"
check_content "src/main/java/com/acme/modres/db/ModResortsCustomerInformation.java" "PostgreSQLDataSourceConfig" "PostgreSQL DataSource usage"
echo ""

echo "4. Checking Database Migration Scripts..."
echo "--------------------------------------"
check_file "src/main/resources/db/migration/V1__create_customer_table.sql"
check_content "src/main/resources/db/migration/V1__create_customer_table.sql" "CREATE TABLE" "Table creation script"
check_content "src/main/resources/db/migration/V1__create_customer_table.sql" "public.customer" "PostgreSQL schema in migration"
echo ""

echo "5. Checking Documentation..."
echo "--------------------------------------"
check_file "POSTGRESQL_MIGRATION.md"
check_file "DATABASE_README.md"
echo ""

echo "6. Checking for SQL Server References..."
echo "--------------------------------------"
if grep -r "sqlserver\|mssql\|SQLServer" src/ pom.xml 2>/dev/null | grep -v "Binary file" > /dev/null; then
    echo -e "${YELLOW}⚠${NC} Warning: Found SQL Server references (may need manual review)"
    ((CHECKS_FAILED++))
else
    echo -e "${GREEN}✓${NC} No SQL Server references found"
    ((CHECKS_PASSED++))
fi
echo ""

echo "=========================================="
echo "Verification Summary"
echo "=========================================="
echo -e "Checks Passed: ${GREEN}${CHECKS_PASSED}${NC}"
echo -e "Checks Failed: ${RED}${CHECKS_FAILED}${NC}"
echo ""

if [ $CHECKS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All verification checks passed!${NC}"
    echo ""
    echo "Next Steps:"
    echo "1. Review database.properties and update connection details"
    echo "2. Create PostgreSQL database: createdb -U postgres modresorts"
    echo "3. Run migration script: psql -U postgres -d modresorts -f src/main/resources/db/migration/V1__create_customer_table.sql"
    echo "4. Build application: mvn clean package"
    echo "5. Deploy and test"
    exit 0
else
    echo -e "${RED}✗ Some verification checks failed. Please review the output above.${NC}"
    exit 1
fi
