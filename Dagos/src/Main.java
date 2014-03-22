import com.sun.media.jai.codec.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Jack on 15.12.13.
 */
public class Main implements ActionListener, ItemListener {

    private JFrame frame;
    private File imageFile = new File("D:\\1.jpg");
    private RenderedImagePanel imagePanel;
    private int currentImgNumber;

    public static void main(String[] args) {
        Main main = new Main();
        main.createForm();
    }

    public void createForm() {
        frame = new JFrame("Dagos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        imagePanel = new RenderedImagePanel();
        imagePanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
                loadImageEx(mouseWheelEvent.getWheelRotation());
            }
        });
        frame.getContentPane().add(imagePanel);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem fileMenuItem = new JMenuItem("Open");
        fileMenuItem.addActionListener(this);
        menu.add(fileMenuItem);

        frame.pack();
        frame.setVisible(true);
    }

    public void loadImageEx(int scrollType) {
        try{
            int pageNum = scrollType + this.currentImgNumber;
            SeekableStream s = new FileSeekableStream(imageFile);
            ImageDecoder decoder = ImageCodec.createImageDecoder("tiff", s, null);
            System.out.println("Pages count: " + decoder.getNumPages());
            int imagesCount = decoder.getNumPages();
            if(pageNum < 0) {
                pageNum = 0;
            } else if (pageNum > imagesCount - 1) {
                pageNum = imagesCount - 1;
            }
            this.currentImgNumber = pageNum;
            System.out.println("Showing image:" + pageNum);
            RenderedImage image = decoder.decodeAsRenderedImage(pageNum);
            imagePanel.setImage(image);
            imagePanel.repaint();

            this.imagePanel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            this.frame.pack();
            this.frame.setLocationRelativeTo(null);
        } catch (IOException e) {

        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JFileChooser fileChooser = new JFileChooser();
        int openResult = fileChooser.showOpenDialog(this.frame);
        if(openResult == JFileChooser.APPROVE_OPTION) {
            this.imageFile = fileChooser.getSelectedFile();
            this.currentImgNumber = 0;
            loadImageEx(0);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {

    }

    class RenderedImagePanel extends JPanel {
        private RenderedImage image;

        public void setImage(RenderedImage image) {
            this.image = image;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (this.image != null) {
                Graphics2D g2 = (Graphics2D) g;
                g2.drawRenderedImage (this.image, AffineTransform.getScaleInstance(1, 1));
            }
        }
    }
}
