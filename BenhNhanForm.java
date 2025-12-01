package view;

import dao.BenhNhanDAO;
import model.BenhNhan;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BenhNhanForm extends JFrame {
    private JTextField txtMaBN, txtHoTen, txtNamSinh, txtDiaChi, txtSoDienThoai, txtChanDoan, txtSearch;
    private JComboBox<String> cbGioiTinh;
    private JTable tblBenhNhan;
    private DefaultTableModel tableModel;
    private JLabel lblCount;
    private BenhNhanDAO dao;

    public BenhNhanForm() {
        dao = new BenhNhanDAO();
        initUI();
        loadTable();
    }

    private void initUI() {
        setTitle("Quản lý phòng khám - Hồ sơ bệnh nhân");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y;
        pnlForm.add(new JLabel("Mã BN*"), gbc);
        gbc.gridx = 1;
        txtMaBN = new JTextField(15);
        pnlForm.add(txtMaBN, gbc);

        gbc.gridx = 2;
        pnlForm.add(new JLabel("Họ tên*"), gbc);
        gbc.gridx = 3;
        txtHoTen = new JTextField(20);
        pnlForm.add(txtHoTen, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        pnlForm.add(new JLabel("Năm sinh*"), gbc);
        gbc.gridx = 1;
        txtNamSinh = new JTextField(10);
        pnlForm.add(txtNamSinh, gbc);

        gbc.gridx = 2;
        pnlForm.add(new JLabel("Giới tính"), gbc);
        gbc.gridx = 3;
        cbGioiTinh = new JComboBox<>(new String[]{"Nam","Nữ"});
        pnlForm.add(cbGioiTinh, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        pnlForm.add(new JLabel("Địa chỉ"), gbc);
        gbc.gridx = 1;
        txtDiaChi = new JTextField(20);
        pnlForm.add(txtDiaChi, gbc);

        gbc.gridx = 2;
        pnlForm.add(new JLabel("Số điện thoại"), gbc);
        gbc.gridx = 3;
        txtSoDienThoai = new JTextField(15);
        pnlForm.add(txtSoDienThoai, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y;
        pnlForm.add(new JLabel("Chẩn đoán*"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtChanDoan = new JTextField();
        pnlForm.add(txtChanDoan, gbc);
        gbc.gridwidth = 1;

        JPanel pnlButtons = new JPanel();
        JButton btnHienThi = new JButton("Hiển thị");
        JButton btnThem = new JButton("Thêm");
        JButton btnCapNhat = new JButton("Cập nhật");
        JButton btnXoa = new JButton("Xóa");
        JButton btnReset = new JButton("Reset");

        pnlButtons.add(btnHienThi);
        pnlButtons.add(btnThem);
        pnlButtons.add(btnCapNhat);
        pnlButtons.add(btnXoa);
        pnlButtons.add(btnReset);

        tableModel = new DefaultTableModel(new Object[]{"Mã BN","Họ tên","Năm sinh","Giới tính","Địa chỉ","SĐT","Chẩn đoán"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblBenhNhan = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblBenhNhan);

        JPanel pnlTop = new JPanel(new BorderLayout());
        JPanel pnlSearch = new JPanel();
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Tìm kiếm");
        pnlSearch.add(new JLabel("Tìm theo MaBN/HoTen: "));
        pnlSearch.add(txtSearch);
        pnlSearch.add(btnSearch);
        pnlTop.add(pnlSearch, BorderLayout.WEST);

        lblCount = new JLabel("Tổng số bệnh nhân: 0");
        pnlTop.add(lblCount, BorderLayout.EAST);

        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());
        Box box = Box.createVerticalBox();
        box.add(pnlForm);
        box.add(pnlButtons);
        box.add(pnlTop);
        box.add(scrollPane);
        cp.add(box, BorderLayout.CENTER);

        // Event handlers
        btnHienThi.addActionListener(e -> loadTable());
        btnReset.addActionListener(e -> resetForm());
        btnThem.addActionListener(e -> addBenhNhan());
        btnCapNhat.addActionListener(e -> updateBenhNhan());
        btnXoa.addActionListener(e -> deleteBenhNhan());
        btnSearch.addActionListener(e -> search());

        tblBenhNhan.getSelectionModel().addListSelectionListener(e -> {
            int row = tblBenhNhan.getSelectedRow();
            if (row >= 0) {
                txtMaBN.setText((String) tableModel.getValueAt(row, 0));
                txtHoTen.setText((String) tableModel.getValueAt(row, 1));
                txtNamSinh.setText(String.valueOf(tableModel.getValueAt(row, 2)));
                cbGioiTinh.setSelectedItem(tableModel.getValueAt(row, 3));
                txtDiaChi.setText((String) tableModel.getValueAt(row, 4));
                txtSoDienThoai.setText((String) tableModel.getValueAt(row, 5));
                txtChanDoan.setText((String) tableModel.getValueAt(row, 6));
                txtMaBN.setEnabled(false);
            }
        });
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        List<BenhNhan> list = dao.getAll();
        for (BenhNhan bn : list) {
            tableModel.addRow(new Object[]{
                    bn.getMaBN(),
                    bn.getHoTen(),
                    bn.getNamSinh(),
                    bn.getGioiTinh(),
                    bn.getDiaChi(),
                    bn.getSoDienThoai(),
                    bn.getChanDoan()
            });
        }
        lblCount.setText("Tổng số bệnh nhân: " + dao.countAll());
    }

    private void resetForm() {
        txtMaBN.setText("");
        txtHoTen.setText("");
        txtNamSinh.setText("");
        cbGioiTinh.setSelectedIndex(0);
        txtDiaChi.setText("");
        txtSoDienThoai.setText("");
        txtChanDoan.setText("");
        txtMaBN.setEnabled(true);
        tblBenhNhan.clearSelection();
    }

    private String validateInput(boolean isInsert) {
        String ma = txtMaBN.getText().trim();
        String ten = txtHoTen.getText().trim();
        String namStr = txtNamSinh.getText().trim();
        String chanDoan = txtChanDoan.getText().trim();

        if (ma.isEmpty()) return "Mã bệnh nhân (MaBN) không được để trống.";
        if (ten.isEmpty()) return "Họ tên không được để trống.";
        if (namStr.isEmpty()) return "Năm sinh không được để trống.";
        int nam;
        try {
            nam = Integer.parseInt(namStr);
            int yearNow = java.time.Year.now().getValue();
            if (nam < 1900 || nam > yearNow) return "Năm sinh không hợp lệ.";
        } catch (NumberFormatException ex) {
            return "Năm sinh phải là số nguyên.";
        }
        if (chanDoan.isEmpty()) return "Chẩn đoán không được để trống.";

        if (isInsert && dao.existsById(ma)) {
            return "Mã bệnh nhân đã tồn tại. Vui lòng nhập mã khác.";
        }

        String sdt = txtSoDienThoai.getText().trim();
        if (!sdt.isEmpty() && !sdt.matches("\\d+")) {
            return "Số điện thoại chỉ được chứa chữ số.";
        }
        return null;
    }

    private void addBenhNhan() {
        String err = validateInput(true);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BenhNhan bn = new BenhNhan(
                txtMaBN.getText().trim(),
                txtHoTen.getText().trim(),
                Integer.parseInt(txtNamSinh.getText().trim()),
                cbGioiTinh.getSelectedItem().toString(),
                txtDiaChi.getText().trim(),
                txtSoDienThoai.getText().trim(),
                txtChanDoan.getText().trim()
        );
        if (dao.insert(bn)) {
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            loadTable();
            resetForm();
        } else {
            JOptionPane.showMessageDialog(this, "Thêm thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBenhNhan() {
        String err = validateInput(false);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        BenhNhan bn = new BenhNhan(
                txtMaBN.getText().trim(),
                txtHoTen.getText().trim(),
                Integer.parseInt(txtNamSinh.getText().trim()),
                cbGioiTinh.getSelectedItem().toString(),
                txtDiaChi.getText().trim(),
                txtSoDienThoai.getText().trim(),
                txtChanDoan.getText().trim()
        );
        if (dao.update(bn)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            loadTable();
            resetForm();
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBenhNhan() {
        String ma = txtMaBN.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn bệnh nhân để xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa bệnh nhân mã " + ma + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(ma)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadTable();
                resetForm();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void search() {
        String key = txtSearch.getText().trim();
        if (key.isEmpty()) {
            loadTable();
            return;
        }
        tableModel.setRowCount(0);
        List<BenhNhan> list = dao.search(key);
        for (BenhNhan bn : list) {
            tableModel.addRow(new Object[]{
                    bn.getMaBN(),
                    bn.getHoTen(),
                    bn.getNamSinh(),
                    bn.getGioiTinh(),
                    bn.getDiaChi(),
                    bn.getSoDienThoai(),
                    bn.getChanDoan()
            });
        }
        lblCount.setText("Tổng kết quả: " + list.size());
    }
}