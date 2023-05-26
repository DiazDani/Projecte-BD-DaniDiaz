package Menu;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MenuTabbed extends JFrame implements ActionListener {

    private double totalPrice = 0.0;
    private Map<String, Double> itemPrices = new HashMap<>();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    private JLabel priceLabel = new JLabel("Total: " + currencyFormat.format(totalPrice));
    private JTextField nameField, priceField, descriptionField;
    private JLabel photoLabel;

    public MenuTabbed() {

        setTitle("Gestio de Barraques");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        itemPrices.put("Butifarra",5.50);
        itemPrices.put("Llom", 4.50);
        itemPrices.put("Pinxo", 4.50);
        itemPrices.put("Coca-Cola", 2.00);
        itemPrices.put("Fanta", 2.00);
        itemPrices.put("Aigua", 1.50);
        itemPrices.put("Cubata", 5.00);
        itemPrices.put("Gin Tonic", 5.00);
        itemPrices.put("Mojito", 6.50);


        JPanel panelPrincipal = new JPanel(new GridLayout(0, 3));


        panelPrincipal.add(createPanel("Entrepans"));
        panelPrincipal.add(createPanel("Begudes"));
        panelPrincipal.add(createPanel("Combinats"));


        JPanel panelPreus = new JPanel(new BorderLayout());
        panelPreus.add(priceLabel, BorderLayout.CENTER);


        JButton finishButton = new JButton("Finalitzar compra");
        finishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                totalPrice = 0.0;
                priceLabel.setText("Total: " + currencyFormat.format(totalPrice));
            }
        });
        panelPreus.add(finishButton, BorderLayout.EAST);


        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(panelPrincipal, BorderLayout.CENTER);
        contentPanel.add(panelPreus, BorderLayout.SOUTH);


        tabbedPane.addTab("Menú", contentPanel);


        JPanel addProductPanel = createAddProductPanel();
        tabbedPane.addTab("Afegir Producte", addProductPanel);


        add(tabbedPane, BorderLayout.CENTER);
        JPanel inventoryPanel = createInventoryPanel();
        tabbedPane.addTab("Inventari", inventoryPanel);
    }

    private JPanel createPanel(String category) {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JLabel label = new JLabel(category);
        label.setHorizontalAlignment(JLabel.CENTER);

        panel.add(label, BorderLayout.NORTH);

        for (String item : itemPrices.keySet()) {
            if (getCategory(item).equals(category)) {
                addButton(panel, item);
            }
        }

        return panel;
    }

    private void addButton(JPanel panel, String text) {
        JButton button = new JButton(text + " - " + currencyFormat.format(itemPrices.get(text)));
        button.addActionListener(this);
        panel.add(button);
    }

    private String getCategory(String item) {
        if (item.startsWith("Butifarra") || item.startsWith("Llom")|| item.startsWith("Pinxo")) {
            return "Entrepans";
        } else if (item.startsWith("Coca-Cola") || item.startsWith("Fanta") || item.startsWith("Aigua")) {
            return "Begudes";
        } else if (item.startsWith("Cubata") || item.startsWith("Gin Tonic") || item.startsWith("Mojito")) {
            return "Combinats";
        } else {
            return "";
        }
    }

    private JPanel createAddProductPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1));

        JLabel nameLabel = new JLabel("Nom:");
        nameField = new JTextField(20);

        JLabel priceLabel = new JLabel("Preu:");
        priceField = new JTextField(10);

        JLabel descriptionLabel = new JLabel("Descripció:");
        descriptionField = new JTextField(50);

        JButton selectPhotoButton = new JButton("Seleccionar foto");
        selectPhotoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(MenuTabbed.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    ImageIcon imageIcon = new ImageIcon(selectedFile.getAbsolutePath());
                    photoLabel.setIcon(imageIcon);
                }
            }
        });

        photoLabel = new JLabel();
        photoLabel.setHorizontalAlignment(JLabel.CENTER);

        JButton addProductButton = new JButton("Agregar producte");
        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                String description = descriptionField.getText();
                ImageIcon imageIcon = (ImageIcon) photoLabel.getIcon();
                Image image = imageIcon.getImage();

                try (Connection conn = getConnection()) {
                    String sql = "INSERT INTO inventari (nom, preu, descripcio, imatge) VALUES (?, ?, ?, ?)";
                    PreparedStatement statement = conn.prepareStatement(sql);
                    statement.setString(1, name);
                    statement.setDouble(2, price);
                    statement.setString(3, description);
                    statement.setBytes(4, getByteArray(image));
                    statement.executeUpdate();

                    JOptionPane.showMessageDialog(MenuTabbed.this, "Producte afegit correctament", "Éxit", JOptionPane.INFORMATION_MESSAGE);
                    clearAddProductFields();
                }catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MenuTabbed.this, "Error al afegir el producte a la base de dades", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(priceLabel);
        panel.add(priceField);
        panel.add(descriptionLabel);
        panel.add(descriptionField);
        panel.add(selectPhotoButton);
        panel.add(photoLabel);
        panel.add(addProductButton);

        return panel;
    }

    private byte[] getByteArray(Image image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bufferedImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();

            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());


        DefaultTableModel tableModel = new DefaultTableModel();
        JTable table = new JTable(tableModel);


        tableModel.addColumn("Nom");
        tableModel.addColumn("Preu");
        tableModel.addColumn("Descripció");
        tableModel.addColumn("Imatge");

        try (Connection conn = getConnection()) {

            String sql = "SELECT nom, preu, descripcio, imatge FROM inventari";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();


            while (resultSet.next()) {
                String name = resultSet.getString("nom");
                double price = resultSet.getDouble("preu");
                String description = resultSet.getString("descripcio");


                byte[] imageData = resultSet.getBytes("imatge");
                ImageIcon imageIcon = new ImageIcon(imageData);
                Image image = imageIcon.getImage();
                Image scaledImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);


                tableModel.addRow(new Object[]{name, price, description, scaledImageIcon});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(MenuTabbed.this, "Error al recuperar els productes de l'inventari", "Error", JOptionPane.ERROR_MESSAGE);
        }

        table.getColumnModel().getColumn(3).setCellRenderer(new ImageIconTableCellRenderer());

        table.getColumnModel().getColumn(3).setCellRenderer(new ImageIconTableCellRenderer());


        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);


        table.setRowHeight(80);


        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        JButton updateButton = new JButton("Actualitzar");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                tableModel.setRowCount(0);

                try (Connection conn = getConnection()) {
                    String sql = "SELECT nom, preu, descripcio, imatge FROM inventari";
                    PreparedStatement statement = conn.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        String name = resultSet.getString("nom");
                        double price = resultSet.getDouble("preu");
                        String description = resultSet.getString("descripcio");
                        byte[] imageData = resultSet.getBytes("imatge");
                        ImageIcon imageIcon = new ImageIcon(imageData);
                        Image image = imageIcon.getImage();
                        Image scaledImage = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                        ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                        tableModel.addRow(new Object[]{name, price, description, scaledImageIcon});
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MenuTabbed.this, "Error al recuperar els productes de l'inventari", "Error", JOptionPane.ERROR_MESSAGE);
                }

                table.repaint();
            }
        });


        panel.add(updateButton, BorderLayout.NORTH);
        return panel;
    }
    private class ImageIconTableCellRenderer extends DefaultTableCellRenderer {
        private static final int IMAGE_WIDTH = 80;
        private static final int IMAGE_HEIGHT = 80;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof ImageIcon) {
                JLabel label = new JLabel();
                ImageIcon imageIcon = (ImageIcon) value;
                Image image = imageIcon.getImage();
                Image scaledImage = image.getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
                ImageIcon scaledImageIcon = new ImageIcon(scaledImage);
                label.setIcon(scaledImageIcon);
                return label;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }


    private Connection getConnection() throws SQLException {
        Connection conn = null;
        try (FileInputStream f = new FileInputStream("C:\\Users\\ddiaz\\IdeaProjects\\proyectoMagdonal\\src\\Model\\connection.properties")) {
            Properties pros = new Properties();
            pros.load(f);
            String url = pros.getProperty("url");
            String user = pros.getProperty("user");
            String password = pros.getProperty("password");
            conn = DriverManager.getConnection(url, user, password);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonText = e.getActionCommand();
        double itemPrice = itemPrices.get(buttonText.substring(0, buttonText.indexOf(" - ")));
        totalPrice += itemPrice;
        priceLabel.setText("Total: " + currencyFormat.format(totalPrice));
    }

    private void clearAddProductFields() {
        nameField.setText("");
        priceField.setText("");
        descriptionField.setText("");
        photoLabel.setIcon(null);
    }

    public static void main(String[] args) {
        MenuTabbed menu = new MenuTabbed();
        menu.setVisible(true);
        try (Connection conn = menu.getConnection()) {
            System.out.println(String.format("Connected to database %s successfully.", conn.getCatalog()));
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
