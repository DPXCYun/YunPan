package com.example.yunpiyuanpan.qrcodeutil;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateQRCodeWithBG {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    public static void generateQRCodeImage(String qrCodeData, int qrCodeWidth, int qrCodeHeight, String filePath)
            throws WriterException, IOException {
        // 1. 修正：移除错误的 EncodeHint/XMLSchema 行
        Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.MARGIN, 1); // 可选：设置白边

        BitMatrix byteMatrix = new MultiFormatWriter().encode(
                qrCodeData,
                BarcodeFormat.QR_CODE,
                qrCodeWidth,
                qrCodeHeight,
                hintMap
        );

        int matrixWidth = byteMatrix.getWidth();
        // 2. 修正：使用 TYPE_INT_RGB（不带透明通道），避免合成问题
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth, BufferedImage.TYPE_INT_RGB);

        // 直接遍历设置像素：黑点 or 白底
        for (int x = 0; x < matrixWidth; x++) {
            for (int y = 0; y < matrixWidth; y++) {
                if (byteMatrix.get(x, y)) {
                    image.setRGB(x, y, Color.BLACK.getRGB()); // 黑色
                } else {
                    image.setRGB(x, y, Color.WHITE.getRGB()); // 白色（非透明！）
                }
            }
        }

        // 不需要再 drawImage 或 fillRect！上面已经完整绘制了图像

        ImageIO.write(image, "png", new File(filePath));
    }

    public static void main(String[] args) {
        String qrData = "https://baidu.com";
        String filePath = "D:/YPFile/qrcode.png";

        try {
            generateQRCodeImage(qrData, WIDTH, HEIGHT, filePath);
            System.out.println("✅ 二维码已生成: " + filePath);
        } catch (WriterException | IOException e) {
            System.err.println("❌ 生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}





//import com.google.zxing.BarcodeFormat;
//import com.google.zxing.EncodeHintType;
//import com.google.zxing.MultiFormatWriter;
//import com.google.zxing.common.BitMatrix;
//import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//public class CreateQRCodeWithBG {
//
//    //文字显示
//    private static final int QRCOLOR = 0x201f1f; // 二维码颜色:黑色
//    private static final int BGWHITE = 0xFFFFFF; //二维码背景颜色:白色
//
//    // 设置QR二维码参数信息
//    private static Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>() {
//        private static final long serialVersionUID = 1L;
//        {
//            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);// 设置QR二维码的纠错级别(H为最高级别)
//            put(EncodeHintType.CHARACTER_SET, "utf-8");// 设置编码方式
//            put(EncodeHintType.MARGIN, 0);// 白边
//        }
//    };
//
//    /**
//     * 生成二维码图片+背景+文字描述
//     * @param codeFile 生成图地址
//     * @param bgImgFile 背景图地址
//     * @param WIDTH 二维码宽度
//     * @param HEIGHT 二维码高度
//     * @param qrUrl 二维码识别地址
//     * @param note  文字描述1
//     * @param tui   文字描述2
//     * @param size 文字大小
//     * @param imagesX 二维码x轴方向
//     * @param imagesY 二维码y轴方向
//     * @param text1X 文字描述1x轴方向
//     * @param text1Y 文字描述1y轴方向
//     * @param text2X 文字描述2x轴方向
//     * @param text2Y 文字描述2y轴方向
//     */
//    public static void CreatQRCode(File codeFile, File bgImgFile, Integer WIDTH, Integer HEIGHT, String qrUrl,
//                                   String note, String tui, Integer size, Integer imagesX, Integer imagesY, Integer text1X, Integer text1Y
//            , Integer text2X, Integer text2Y) {
//        try {
//            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
//            // 参数顺序分别为: 编码内容,编码类型,生成图片宽度,生成图片高度,设置参数
//            BitMatrix bm = multiFormatWriter.encode(qrUrl, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
//            BufferedImage image = new BufferedImage(WIDTH, HEIGHT,BufferedImage.TYPE_INT_RGB);
//
//            // 开始利用二维码数据创建Bitmap图片，分别设为黑(0xFFFFFFFF) 白(0xFF000000)两色
//            for (int x = 0; x < WIDTH; x++) {
//                for (int y = 0; y < HEIGHT; y++) {
//                    image.setRGB(x, y, bm.get(x, y) ? QRCOLOR : BGWHITE);
//                }
//            }
//
//            //添加背景图片
//            BufferedImage backgroundImage = ImageIO.read(bgImgFile);
//            int bgWidth=backgroundImage.getWidth();//获得图片宽度
//            int qrWidth=image.getWidth();//获得二维码宽度
//            //距离背景图片x边的距离，居中显示
////            int disx=(bgWidth-qrWidth)-imagesX;//imagesX为二维码x轴方方向
//            int disx = (bgWidth-qrWidth)/2;
//            //距离y边距离 * * * *
//            int disy=imagesY;//imagesY为二维码y轴方向
//            Graphics2D rng=backgroundImage.createGraphics();
//            rng.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
//            rng.drawImage(image,disx,disy,WIDTH,HEIGHT,null);//image是二维码图片
//
//            /*
//             * 	文字描述参数设置
//             */
//
//            Color textColor=Color.white;
//            rng.setColor(textColor);
//            rng.drawImage(backgroundImage,0,0,null);
//            //设置字体类型和大小(BOLD加粗/ PLAIN平常)
//            rng.setFont(new Font("微软雅黑,Arial",Font.BOLD,size));
//            //设置字体颜色
//            rng.setColor(Color.black);
//            int strWidth=rng.getFontMetrics().stringWidth(note);
//
//            //文字1显示位置
//            int disx1=(bgWidth-strWidth)-text1X;//左右
//            rng.drawString(note,disx1,text1Y);//上下
//
//            //文字2显示位置
//            int disx2=(bgWidth-strWidth)-text2X;//左右
//            rng.drawString(tui,disx2,text2Y);//上下
//
//            rng.dispose();
//            image=backgroundImage;
//            image.flush();
//            ImageIO.write(image, "png", codeFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 测试
//     * @param args
//     */
//    public static void main(String[] args)  {
//
//        //发布到服务器后，应设置为服务器上背景图片的位置
//        File bgImgFile=new File("D:/YPFile/bg.jpg");//背景图片
//        File QrCodeFile = new File("D:/YPFile/qrcodewithbg.png");//生成图片位置
//        String url = "https://blog.csdn.net/qq_53336526";//二维码链接
//        String note = "" ;//文字描述
//        String tui = "" ;//文字描述
//
//
//        //宣传二维码生成
//        //           生成图地址, 背景图地址,   二维码宽度,    二维码高度,二维码识别地址,文字描述1,文字描述2,文字大小, 二维码x轴方向,  二维码y轴方向,           文字1||2xy轴方向
//        CreatQRCode(QrCodeFile,bgImgFile, 400, 400,    url,      note,    tui, 38, 408, 508, 0, 0, 410, 210);
//
//    }
//
//}
