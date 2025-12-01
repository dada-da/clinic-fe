package model;

public class BenhNhan {
    private String maBN;
    private String hoTen;
    private int namSinh;
    private String gioiTinh;
    private String diaChi;
    private String soDienThoai;
    private String chanDoan;

    public BenhNhan() {}

    public BenhNhan(String maBN, String hoTen, int namSinh, String gioiTinh, String diaChi, String soDienThoai, String chanDoan) {
        this.maBN = maBN;
        this.hoTen = hoTen;
        this.namSinh = namSinh;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
        this.soDienThoai = soDienThoai;
        this.chanDoan = chanDoan;
    }
    // getters & setters
    public String getMaBN() { return maBN; }
    public void setMaBN(String maBN) { this.maBN = maBN; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public int getNamSinh() { return namSinh; }
    public void setNamSinh(int namSinh) { this.namSinh = namSinh; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getChanDoan() { return chanDoan; }
    public void setChanDoan(String chanDoan) { this.chanDoan = chanDoan; }
}