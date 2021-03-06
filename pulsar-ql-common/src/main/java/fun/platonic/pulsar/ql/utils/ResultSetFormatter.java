package fun.platonic.pulsar.ql.utils;

import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copy from org.h2.tools.Shell
 * */
public class ResultSetFormatter {
    public static int MAX_ROW_BUFFER = 5000;
    public static int MAX_COLUMN_LENGTH = 1000;
    public static char BOX_VERTICAL = '|';
    private ResultSet rs;
    private int rowCount;

    public ResultSetFormatter(ResultSet rs) {
        this.rs = rs;
    }

    public String format() {
        return format(false);
    }

    public String format(boolean asList) {
        try {
            if (asList) {
                return getResultAsList();
            }
            return getResultAsTable();
        } catch (SQLException e) {
            return "(Exception)";
        }
    }

    public String formatAsLine() {
        return format(true);
    }

    private String getResultAsTable() throws SQLException {
        StringBuilder sb = new StringBuilder();

        ResultSetMetaData meta = rs.getMetaData();
        int len = meta.getColumnCount();
        ArrayList<String[]> rows = new ArrayList<>();
        // buffer the header
        String[] columns = new String[len];
        for (int i = 0; i < len; i++) {
            String s = meta.getColumnLabel(i + 1);
            columns[i] = s == null ? "" : s;
        }
        rows.add(columns);
        while (rs.next()) {
            rowCount++;
            loadRow(len, rows);
            if (rowCount > MAX_ROW_BUFFER) {
                sb.append(formatRows(rows, len));
                sb.append("\n");
                rows.clear();
            }
        }

        sb.append(formatRows(rows, len));
        sb.append("\n");
        rows.clear();

        return sb.toString();
    }

    private boolean loadRow(int len, ArrayList<String[]> rows) throws SQLException {
        boolean truncated = false;
        String[] row = new String[len];
        int i = 0;
        while (i < len) {
            String s = rs.getString(i + 1);
            if (s == null) {
                s = "null";
            }

            // only truncate if more than one column
            if (len > 1 && s.length() > MAX_COLUMN_LENGTH) {
                s = s.substring(0, MAX_COLUMN_LENGTH);
                s += " ...";
                truncated = true;
            }
            row[i] = s;

            ++i;
        }
        rows.add(row);
        return truncated;
    }

    private String formatRows(final ArrayList<String[]> rows, final int len) {
        int[] columnSizes = new int[len];
        for (int i = 0; i < len; i++) {
            int max = 0;
            for (String[] row : rows) {
                max = Math.max(max, row[i].length());
            }
            if (len > 1) {
                max = Math.min(MAX_COLUMN_LENGTH, max);
            }
            columnSizes[i] = max;
        }

        StringBuilder buff = new StringBuilder();
        for (String[] row : rows) {
            // IntStream.range(0, len).mapToObj(i -> row[i]).collect(Collectors.joining(" ", "|", " "));
            int i = 0;
            while (i < len) {
                if (i > 0) {
                    buff.append(' ').append(BOX_VERTICAL).append(' ');
                }

                String s = row[i];
                buff.append(s);
                if (i < len - 1) {
                    for (int j = s.length(); j < columnSizes[i]; j++) {
                        buff.append(' ');
                    }
                }

                ++i;
            }

            buff.append("\n");
        }

        return buff.toString();
    }

    private String getResultAsList() throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int longestLabel = 0;
        final int len = meta.getColumnCount();
        String[] columns = new String[len];
        for (int i = 0; i < len; i++) {
            String s = meta.getColumnLabel(i + 1);
            columns[i] = s;
            longestLabel = Math.max(longestLabel, s.length());
        }

        StringBuilder sb = new StringBuilder();
        while (rs.next()) {
            rowCount++;
            sb.setLength(0);
            if (rowCount > 1) {
                sb.append("");
            }

            for (int i = 0; i < len; ++i) {
                if (i > 0) {
                    sb.append('\n');
                }

                sb.append(StringUtils.rightPad(columns[i] + ":", 15 + longestLabel))
                        .append(rs.getString(i + 1));
            }
            sb.append("\n");
        }
        if (rowCount == 0) {
            String s = Stream.of(columns).collect(Collectors.joining("\n"));
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return format() + "Total " + rowCount + " rows";
    }
}
