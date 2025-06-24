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
        
        public ConversionResult(String preparedSql, List<Object> parameters) {
            this.preparedSql = preparedSql;
            this.parameters = parameters;
        }
        
        public String getPreparedSql() { return preparedSql; }
        public List<Object> getParameters() { return parameters; }
    }
    
    /**
     * Generic method to convert any SQL string to PreparedStatement format
     */
    public static ConversionResult convertSqlToPrepared(String originalSql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(originalSql);
        
        List<Object> parameters = new ArrayList<>();
        
        // Custom expression deparser that replaces literals with ?
        ExpressionDeParser expressionDeParser = new ExpressionDeParser() {
            @Override
            public void visit(StringValue stringValue) {
                parameters.add(stringValue.getValue());
                buffer.append("?");
            }
            
            @Override
            public void visit(LongValue longValue) {
                parameters.add(longValue.getValue());
                buffer.append("?");
            }
            
            @Override
            public void visit(DoubleValue doubleValue) {
                parameters.add(doubleValue.getValue());
                buffer.append("?");
            }
            
            @Override
            public void visit(DateValue dateValue) {
                parameters.add(dateValue.getValue());
                buffer.append("?");
            }
            
            @Override
            public void visit(TimeValue timeValue) {
                parameters.add(timeValue.getValue());
                buffer.append("?");
            }
            
            @Override
            public void visit(TimestampValue timestampValue) {
                parameters.add(timestampValue.getValue());
                buffer.append("?");
            }
            
            @Override
            public void visit(NullValue nullValue) {
                parameters.add(null);
                buffer.append("?");
            }
            
            @Override
            public void visit(HexValue hexValue) {
                parameters.add(hexValue.getValue());
                buffer.append("?");
            }
        };
        
        SelectDeParser selectDeParser = new SelectDeParser(expressionDeParser, new StringBuilder());
        expressionDeParser.setSelectVisitor(selectDeParser);
        expressionDeParser.setBuffer(new StringBuilder());
        
        StatementDeParser statementDeParser = new StatementDeParser(expressionDeParser, selectDeParser, new StringBuilder());
        statement.accept(statementDeParser);
        
        String preparedSql = statementDeParser.getBuffer().toString();
        
        return new ConversionResult(preparedSql, parameters);
    }
    
    /**
     * Create a fully configured PreparedStatement
     */
    public static PreparedStatement createPreparedStatement(Connection connection, String originalSql) 
            throws Exception, SQLException {
        ConversionResult result = convertSqlToPrepared(originalSql);
        
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