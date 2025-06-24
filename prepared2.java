import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlToPreparedStatementConverter {
    
    public static class ConversionResult {
        private final String preparedSql;
        private final List<Object> parameters;
        private final List<String> warnings;
        
        public ConversionResult(String preparedSql, List<Object> parameters, List<String> warnings) {
            this.preparedSql = preparedSql;
            this.parameters = parameters;
            this.warnings = warnings;
        }
        
        public String getPreparedSql() { return preparedSql; }
        public List<Object> getParameters() { return parameters; }
        public List<String> getWarnings() { return warnings; }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
    }
    
    /**
     * Generic method to convert any SQL string to PreparedStatement format
     * with SQL injection prevention checks
     */
    public static ConversionResult convertSqlToPrepared(String originalSql) throws Exception {
        // First, validate the SQL for potential injection patterns
        List<String> warnings = validateSqlForInjection(originalSql);
        
        Statement statement = CCJSqlParserUtil.parse(originalSql);
        
        List<Object> parameters = new ArrayList<>();
        
        // Create buffer for the expression deparser
        StringBuilder buffer = new StringBuilder();
        
        // Custom expression deparser that replaces literals with ?
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public void visit(StringValue stringValue) {
                parameters.add(stringValue.getValue());
                this.getBuffer().append("?");
            }
            
            @Override
            public void visit(LongValue longValue) {
                parameters.add(longValue.getValue());
                this.getBuffer().append("?");
            }
            
            @Override
            public void visit(DoubleValue doubleValue) {
                parameters.add(doubleValue.getValue());
                this.getBuffer().append("?");
            }
            
            @Override
            public void visit(DateValue dateValue) {
                parameters.add(dateValue.getValue());
                this.getBuffer().append("?");
            }
            
            @Override
            public void visit(TimeValue timeValue) {
                parameters.add(timeValue.getValue());
                this.getBuffer().append("?");
            }
            
            @Override
            public void visit(TimestampValue timestampValue) {
                parameters.add(timestampValue.getValue());
                this.getBuffer().append("?");
            }
            
            @Override
            public void visit(NullValue nullValue) {
                parameters.add(null);
                this.getBuffer().append("?");
            }
            
            @Override
            public void visit(HexValue hexValue) {
                parameters.add(hexValue.getValue());
                this.getBuffer().append("?");
            }
        };
        
        SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, buffer);
        expressionDeParser.setSelectVisitor(selectDeParser);
        expressionDeParser.setBuffer(buffer);
        
        StatementDeParser statementDeParser = new StatementDeParser(expressionDeParser, selectDeParser, buffer);
        statement.accept(statementDeParser);
        
        String preparedSql = statementDeParser.getBuffer().toString();
        
        return new ConversionResult(preparedSql, parameters, warnings);
    }
    
    /**
     * Validate SQL for potential injection patterns that JSqlParser might not catch
     */
    private static List<String> validateSqlForInjection(String sql) {
        List<String> warnings = new ArrayList<>();
        String sqlLower = sql.toLowerCase();
        
        // Check for common SQL injection patterns
        String[] suspiciousPatterns = {
            "union.*select",
            "';.*--",
            "';.*drop",
            "';.*delete",
            "';.*insert",
            "';.*update",
            "benchmark\\s*\\(",
            "sleep\\s*\\(",
            "waitfor\\s+delay",
            "xp_cmdshell",
            "sp_executesql",
            "exec\\s*\\(",
            "execute\\s*\\(",
            "load_file\\s*\\(",
            "into\\s+outfile",
            "information_schema",
            "pg_sleep\\s*\\(",
            "dbms_pipe.receive_message"
        };
        
        for (String pattern : suspiciousPatterns) {
            if (sqlLower.matches(".*" + pattern + ".*")) {
                warnings.add("Potentially suspicious pattern detected: " + pattern);
            }
        }
        
        // Check for excessive semicolons (stacked queries)
        long semicolonCount = sql.chars().filter(ch -> ch == ';').count();
        if (semicolonCount > 1) {
            warnings.add("Multiple semicolons detected - possible stacked query injection");
        }
        
        // Check for comment patterns that could hide malicious code
        if (sqlLower.contains("/*") || sqlLower.contains("--") || sqlLower.contains("#")) {
            warnings.add("SQL comments detected - review for potential injection");
        }
        
        return warnings;
    }
    
    /**
     * Create a fully configured PreparedStatement with security validation
     */
    public static PreparedStatement createPreparedStatement(Connection connection, String originalSql) 
            throws Exception, SQLException {
        ConversionResult result = convertSqlToPrepared(originalSql);
        
        // Log warnings for security review
        if (result.hasWarnings()) {
            System.err.println("SQL Security Warnings for: " + originalSql);
            result.getWarnings().forEach(warning -> System.err.println("  - " + warning));
            // In production, you might want to log these or even reject the query
        }
        
        PreparedStatement pstmt = connection.prepareStatement(result.getPreparedSql());
        
        // Set parameters with type detection
        for (int i = 0; i < result.getParameters().size(); i++) {
            Object param = result.getParameters().get(i);
            setParameter(pstmt, i + 1, param);
        }
        
        return pstmt;
    }
    
    /**
     * Generic parameter setter with type detection
     */
    private static void setParameter(PreparedStatement pstmt, int index, Object value) throws SQLException {
        if (value == null) {
            pstmt.setNull(index, java.sql.Types.NULL);
        } else if (value instanceof String) {
            pstmt.setString(index, (String) value);
        } else if (value instanceof Integer) {
            pstmt.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            pstmt.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            pstmt.setDouble(index, (Double) value);
        } else if (value instanceof java.sql.Date) {
            pstmt.setDate(index, (java.sql.Date) value);
        } else if (value instanceof java.sql.Time) {
            pstmt.setTime(index, (java.sql.Time) value);
        } else if (value instanceof java.sql.Timestamp) {
            pstmt.setTimestamp(index, (java.sql.Timestamp) value);
        } else if (value instanceof Boolean) {
            pstmt.setBoolean(index, (Boolean) value);
        } else {
            // Fallback to setObject for other types
            pstmt.setObject(index, value);
        }
    }
    
    // Example usage and testing
    public static void main(String[] args) {
        try {
            // Test with complex SQL
            String complexSql = "SELECT u.name, p.title, COUNT(*) as cnt " +
                              "FROM users u " +
                              "JOIN profiles p ON u.id = p.user_id " +
                              "LEFT JOIN orders o ON u.id = o.customer_id " +
                              "WHERE u.age > 25 AND p.status = 'active' " +
                              "AND o.created_date >= '2023-01-01' " +
                              "AND u.salary BETWEEN 50000.0 AND 100000.0 " +
                              "GROUP BY u.name, p.title " +
                              "HAVING COUNT(*) > 5";
            
            ConversionResult result = convertSqlToPrepared(complexSql);
            
            System.out.println("Original SQL:");
            System.out.println(complexSql);
            System.out.println("\nPrepared SQL:");
            System.out.println(result.getPreparedSql());
            System.out.println("\nParameters:");
            for (int i = 0; i < result.getParameters().size(); i++) {
                System.out.println((i + 1) + ": " + result.getParameters().get(i) + 
                                 " (" + result.getParameters().get(i).getClass().getSimpleName() + ")");
            }
            
            // Test with potentially malicious SQL
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Testing with potentially malicious SQL:");
            
            String maliciousSql = "SELECT * FROM users WHERE id = 1; DROP TABLE users; --";
            try {
                ConversionResult maliciousResult = convertSqlToPrepared(maliciousSql);
                System.out.println("Malicious SQL: " + maliciousSql);
                System.out.println("Converted: " + maliciousResult.getPreparedSql());
                if (maliciousResult.hasWarnings()) {
                    System.out.println("SECURITY WARNINGS:");
                    maliciousResult.getWarnings().forEach(w -> System.out.println("  - " + w));
                }
            } catch (Exception e) {
                System.out.println("Failed to parse malicious SQL (this is good): " + e.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Usage example with database connection:
/*
public class DatabaseExample {
    public void executeQuery(Connection connection, String sql) throws Exception, SQLException {
        try (PreparedStatement pstmt = SqlToPreparedStatementConverter.createPreparedStatement(connection, sql)) {
            ResultSet rs = pstmt.executeQuery();
            // Process results...
        }
    }
}
*/