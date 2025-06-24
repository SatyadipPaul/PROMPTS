import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class SqlToPreparedStatementConverter {
    
    @Value
    @Builder
    public static class ConversionResult {
        String preparedSql;
        List<Object> parameters;
        @Builder.Default
        List<String> warnings = new ArrayList<>();
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }
    
    // Pre-compiled patterns for better performance (JDK 21 optimizations)
    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
        Pattern.compile("union.*select", Pattern.CASE_INSENSITIVE),
        Pattern.compile("';.*--", Pattern.CASE_INSENSITIVE),
        Pattern.compile("';.*(drop|delete|insert|update)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("benchmark\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("sleep\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("waitfor\\s+delay", Pattern.CASE_INSENSITIVE),
        Pattern.compile("xp_cmdshell", Pattern.CASE_INSENSITIVE),
        Pattern.compile("sp_executesql", Pattern.CASE_INSENSITIVE),
        Pattern.compile("exec(ute)?\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("load_file\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("into\\s+outfile", Pattern.CASE_INSENSITIVE),
        Pattern.compile("information_schema", Pattern.CASE_INSENSITIVE),
        Pattern.compile("pg_sleep\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("dbms_pipe\\.receive_message", Pattern.CASE_INSENSITIVE)
    );
    
    /**
     * Generic method to convert any SQL string to PreparedStatement format
     * with SQL injection prevention checks
     */
    public static ConversionResult convertSqlToPrepared(String originalSql) throws Exception {
        var warnings = validateSqlForInjection(originalSql);
        var statement = CCJSqlParserUtil.parse(originalSql);
        var parameters = new ArrayList<>();
        var buffer = new StringBuilder();
        
        // Modern switch expression for parameter extraction (JDK 21)
        var expressionDeParser = new ExpressionDeParser() {
            private void addParameter(Object value) {
                parameters.add(value);
                this.getBuffer().append("?");
            }
            
            @Override public void visit(StringValue stringValue) { addParameter(stringValue.getValue()); }
            @Override public void visit(LongValue longValue) { addParameter(longValue.getValue()); }
            @Override public void visit(DoubleValue doubleValue) { addParameter(doubleValue.getValue()); }
            @Override public void visit(DateValue dateValue) { addParameter(dateValue.getValue()); }
            @Override public void visit(TimeValue timeValue) { addParameter(timeValue.getValue()); }
            @Override public void visit(TimestampValue timestampValue) { addParameter(timestampValue.getValue()); }
            @Override public void visit(NullValue nullValue) { addParameter(null); }
            @Override public void visit(HexValue hexValue) { addParameter(hexValue.getValue()); }
        };
        
        var selectDeParser = new SelectDeParser(expressionDeParser, buffer);
        expressionDeParser.setSelectVisitor(selectDeParser);
        expressionDeParser.setBuffer(buffer);
        
        var statementDeParser = new StatementDeParser(expressionDeParser, selectDeParser, buffer);
        statement.accept(statementDeParser);
        
        return ConversionResult.builder()
            .preparedSql(statementDeParser.getBuffer().toString())
            .parameters(parameters)
            .warnings(warnings)
            .build();
    }
    
    /**
     * Validate SQL for potential injection patterns using modern Java features
     */
    private static List<String> validateSqlForInjection(String sql) {
        var warnings = new ArrayList<String>();
        
        // Pattern matching with modern Java (JDK 21)
        SUSPICIOUS_PATTERNS.parallelStream()
            .filter(pattern -> pattern.matcher(sql).find())
            .map(pattern -> STR."Potentially suspicious pattern detected: \{pattern.pattern()}")
            .forEach(warnings::add);
        
        // Enhanced validation using switch expressions
        var semicolonCount = sql.chars().filter(ch -> ch == ';').count();
        switch ((int) semicolonCount) {
            case 0, 1 -> { /* Normal case */ }
            default -> warnings.add("Multiple semicolons detected - possible stacked query injection");
        }
        
        // Modern string operations
        var hasComments = sql.contains("/*") || sql.contains("--") || sql.contains("#");
        if (hasComments) {
            warnings.add("SQL comments detected - review for potential injection");
        }
        
        return warnings;
    }
    
    /**
     * Create a fully configured PreparedStatement with security validation
     */
    public static PreparedStatement createPreparedStatement(Connection connection, String originalSql) 
            throws Exception, SQLException {
        var result = convertSqlToPrepared(originalSql);
        
        // Enhanced logging with structured output
        if (result.hasWarnings()) {
            log.warn("SQL Security Warnings for: {}", originalSql);
            result.getWarnings().forEach(warning -> log.warn("  - {}", warning));
        }
        
        var pstmt = connection.prepareStatement(result.getPreparedSql());
        
        // Set parameters using enhanced for loop with index (JDK 21)
        var params = result.getParameters();
        for (int i = 0; i < params.size(); i++) {
            setParameter(pstmt, i + 1, params.get(i));
        }
        
        return pstmt;
    }
    
    /**
     * Generic parameter setter with enhanced pattern matching (JDK 21)
     */
    private static void setParameter(PreparedStatement pstmt, int index, Object value) throws SQLException {
        switch (value) {
            case null -> pstmt.setNull(index, java.sql.Types.NULL);
            case String s -> pstmt.setString(index, s);
            case Integer i -> pstmt.setInt(index, i);
            case Long l -> pstmt.setLong(index, l);
            case Double d -> pstmt.setDouble(index, d);
            case java.sql.Date date -> pstmt.setDate(index, date);
            case java.sql.Time time -> pstmt.setTime(index, time);
            case java.sql.Timestamp ts -> pstmt.setTimestamp(index, ts);
            case Boolean b -> pstmt.setBoolean(index, b);
            default -> pstmt.setObject(index, value);
        }
    }
    
    // Demonstration with modern Java features
    public static void main(String[] args) {
        try {
            // Using text blocks for better readability (JDK 21)
            var complexSql = """
                SELECT u.name, p.title, COUNT(*) as cnt
                FROM users u
                JOIN profiles p ON u.id = p.user_id
                LEFT JOIN orders o ON u.id = o.customer_id
                WHERE u.age > 25 AND p.status = 'active'
                AND o.created_date >= '2023-01-01'
                AND u.salary BETWEEN 50000.0 AND 100000.0
                GROUP BY u.name, p.title
                HAVING COUNT(*) > 5
                """;
            
            var result = convertSqlToPrepared(complexSql);
            
            System.out.println("Original SQL:");
            System.out.println(complexSql);
            System.out.println("\nPrepared SQL:");
            System.out.println(result.getPreparedSql());
            System.out.println("\nParameters:");
            
            // Enhanced parameter display with modern formatting
            var params = result.getParameters();
            for (int i = 0; i < params.size(); i++) {
                var param = params.get(i);
                System.out.println(STR."\{i + 1}: \{param} (\{param.getClass().getSimpleName()})");
            }
            
            // Testing with potentially malicious SQL
            System.out.println("\n" + "=".repeat(50));
            System.out.println("Testing with potentially malicious SQL:");
            
            var maliciousSql = "SELECT * FROM users WHERE id = 1; DROP TABLE users; --";
            try {
                var maliciousResult = convertSqlToPrepared(maliciousSql);
                System.out.println(STR."Malicious SQL: \{maliciousSql}");
                System.out.println(STR."Converted: \{maliciousResult.getPreparedSql()}");
                
                if (maliciousResult.hasWarnings()) {
                    System.out.println("SECURITY WARNINGS:");
                    maliciousResult.getWarnings().forEach(w -> System.out.println(STR."  - \{w}"));
                }
            } catch (Exception e) {
                System.out.println(STR."Failed to parse malicious SQL (this is good): \{e.getMessage()}");
            }
            
        } catch (Exception e) {
            log.error("Error in main method", e);
        }
    }
}

/*
Usage example with modern Java features:

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseService {
    
    private final DataSource dataSource;
    
    public void executeSecureQuery(String sql) {
        try (var connection = dataSource.getConnection();
             var pstmt = SqlToPreparedStatementConverter.createPreparedStatement(connection, sql);
             var rs = pstmt.executeQuery()) {
            
            // Process results with modern Java features
            while (rs.next()) {
                // Process row
            }
            
        } catch (Exception e) {
            log.error("Database operation failed for SQL: {}", sql, e);
            throw new DatabaseException("Query execution failed", e);
        }
    }
}
*/