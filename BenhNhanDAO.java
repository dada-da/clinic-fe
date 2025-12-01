package dao;

import model.BenhNhan;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BenhNhanDAO {

    public BenhNhanDAO() {
        createTableIfNotExists();
    }

    public void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS BenhNhan (" +
                "MaBN TEXT PRIMARY KEY," +
                "HoTen TEXT NOT NULL," +
                "NamSinh INTEGER NOT NULL," +
                "GioiTinh TEXT," +
                "DiaChi TEXT," +
                "SoDienThoai TEXT," +
                "ChanDoan TEXT NOT NULL" +
                ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<BenhNhan> getAll() {
        List<BenhNhan> list = new ArrayList<>();
        String sql = "SELECT * FROM BenhNhan";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                BenhNhan bn = new BenhNhan(
                        rs.getString("MaBN"),
                        rs.getString("HoTen"),
                        rs.getInt("NamSinh"),
                        rs.getString("GioiTinh"),
                        rs.getString("DiaChi"),
                        rs.getString("SoDienThoai"),
                        rs.getString("ChanDoan")
                );
                list.add(bn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(BenhNhan bn) {
        String sql = "INSERT INTO BenhNhan (MaBN, HoTen, NamSinh, GioiTinh, DiaChi, SoDienThoai, ChanDoan) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, bn.getMaBN());
            pst.setString(2, bn.getHoTen());
            pst.setInt(3, bn.getNamSinh());
            pst.setString(4, bn.getGioiTinh());
            pst.setString(5, bn.getDiaChi());
            pst.setString(6, bn.getSoDienThoai());
            pst.setString(7, bn.getChanDoan());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(BenhNhan bn) {
        String sql = "UPDATE BenhNhan SET HoTen=?, NamSinh=?, GioiTinh=?, DiaChi=?, SoDienThoai=?, ChanDoan=? WHERE MaBN=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, bn.getHoTen());
            pst.setInt(2, bn.getNamSinh());
            pst.setString(3, bn.getGioiTinh());
            pst.setString(4, bn.getDiaChi());
            pst.setString(5, bn.getSoDienThoai());
            pst.setString(6, bn.getChanDoan());
            pst.setString(7, bn.getMaBN());
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(String maBN) {
        String sql = "DELETE FROM BenhNhan WHERE MaBN=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maBN);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsById(String maBN) {
        String sql = "SELECT 1 FROM BenhNhan WHERE MaBN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maBN);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BenhNhan> search(String keyword) {
        List<BenhNhan> list = new ArrayList<>();
        String sql = "SELECT * FROM BenhNhan WHERE MaBN LIKE ? OR HoTen LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            pst.setString(1, like);
            pst.setString(2, like);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    BenhNhan bn = new BenhNhan(
                            rs.getString("MaBN"),
                            rs.getString("HoTen"),
                            rs.getInt("NamSinh"),
                            rs.getString("GioiTinh"),
                            rs.getString("DiaChi"),
                            rs.getString("SoDienThoai"),
                            rs.getString("ChanDoan")
                    );
                    list.add(bn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) AS cnt FROM BenhNhan";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("cnt");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}