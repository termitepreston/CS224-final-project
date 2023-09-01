package io.github.termitepreston.schoolprojects.model;

import io.github.termitepreston.schoolprojects.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class ContentTypes {
    private final static String getContentTypesQuery = """
            select
                *
            from
                type;
            """;
    private final DB db;
    private final HashMap<Integer, String> contentTypes;

    public ContentTypes(DB db) {
        this.db = db;

        contentTypes = new HashMap<>();
    }

    public HashMap<Integer, String> getContentTypes() {
        return contentTypes;
    }

    public void loadContentTypes() throws SQLException {
        try (Connection conn = db.getConn()) {
            PreparedStatement stat = conn.prepareStatement(getContentTypesQuery);

            try (ResultSet rs = stat.executeQuery()) {
                while (rs.next()) {
                    contentTypes.put(rs.getInt(1), rs.getString(2));
                }
            }
        }
    }

}
