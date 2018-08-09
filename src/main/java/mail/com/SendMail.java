/**
 * FileName: SendMail
 * Author:   Administrator
 * Date:     2018 年 8 月 8 日 0008 15:30:56
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package mail.com;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * 〈一句话功能简述〉<br> 
 * 〈〉
 *
 * @author Administrator
 * @create 2018 年 8 月 8 日 0008
 * @since 1.0.0
 */
public class SendMail {
    /** 发件人地址 **/
    public static String senderAddress;
    /** 发件人账户名 **/
    public static String senderAccount;
    /** 发件人账户密码 **/
    public static String senderPassword;
    /** 收件人地址 **/
    public static String recipientAddress;
    /** 发送人信息配置 **/
    private static Properties properties;
    /** 连接邮件服务器的参数配置 **/
    private static Properties serviceProperties;

    static {
        properties=new Properties();
        serviceProperties=new Properties();
        try {
            properties.load(SendMail.class.getResourceAsStream("/mail.properties"));
            senderAddress=properties.getProperty("sender.address");
            senderAccount=properties.getProperty("sender.account");
            senderPassword=properties.getProperty("sender.password");
            recipientAddress=properties.getProperty("recipient.address");

            //配置邮件服务器参数
            serviceProperties.load(SendMail.class.getResourceAsStream("/mailService.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws MessagingException, UnsupportedEncodingException {
        //创建定义整个应用程序所需的环境信息的session对象
        Session session=Session.getInstance(serviceProperties);
        //设置在控制台打印调试信息
        session.setDebug(true);

        //创建邮件实例对象
        //Message message=getMimeMessage(session);
        Message message=getMultipartMessage(session);

        //根据session获取邮件传输对象
        Transport transport=session.getTransport();
        //设置发件人的账户名和密码
        transport.connect(senderAccount,senderPassword);
        //发送邮件，并发送到所有收件人地址
        transport.sendMessage(message,message.getAllRecipients());
        //关闭邮件连接
        transport.close();
    }

    /**
     * 获取普通文本邮件对象
     * @param session 环境信息对象
     * @return 邮件对象
     * @throws MessagingException
     */
    private static Message getMimeMessage(Session session) throws MessagingException {
        //创建一封邮件的实例对象
        MimeMessage mimeMessage=new MimeMessage(session);
        //设置发件人地址
        mimeMessage.setFrom(new InternetAddress(senderAddress));
        //设置收件人地址
        mimeMessage.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
        //设置邮件主题
        mimeMessage.setSubject("邮件主题","UTF-8");
        //设置邮件正文
        mimeMessage.setContent("邮件发送成功.","text/html;charset=UTF-8");
        //设置邮件发送时间，默认会立即发送
        mimeMessage.setSentDate(new Date());

        return mimeMessage;
    }

    /**
     * 获取包含文件和附件的邮件对象
     * @param session
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    private static Message getMultipartMessage(Session session) throws MessagingException, UnsupportedEncodingException {
        //创建邮件实例对象
        MimeMessage mimeMessage=new MimeMessage(session);
        //设置发件人地址
        mimeMessage.setFrom(new InternetAddress(senderAddress));
        //设置收件人地址
        mimeMessage.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(recipientAddress));
        //设置邮件主题
        mimeMessage.setSubject("含附件邮件","UTF-8");

        //创建图片节点
        MimeBodyPart partPic=new MimeBodyPart();
        //读取本地文件
        DataHandler dataHandler=new DataHandler(new FileDataSource("src\\main\\resources\\mail.jpg"));
        //将图片数据添加到节点
        partPic.setDataHandler(dataHandler);
        //为节点设置一个唯一编号（在文本节点将引用改id）
        partPic.setContentID("mailPic");

        //创建文本节点
        MimeBodyPart partText=new MimeBodyPart();
        //这里添加图片的方式是将整个图片包含到邮件内容中，实际上也可以使用http连接的方式添加网络图片
        partText.setContent("这是一张图片<br/><a href=''><img src='cid:mailPic'/></a>","text/html;charset=UTF-8");

        //组合图片和文本关系节点
        MimeMultipart multipartTextPic=new MimeMultipart();
        multipartTextPic.addBodyPart(partText);
        multipartTextPic.addBodyPart(partPic);
        //关联关系
        multipartTextPic.setSubType("related");

        //将组合节点封装成普通节点
        //最终添加到邮件的content是由多个BodyPart组成的Multipart，所以需要的是BodyPart
        MimeBodyPart partTextPic=new MimeBodyPart();
        partTextPic.setContent(multipartTextPic);

        //创键附件节点
        MimeBodyPart partAttachment=new MimeBodyPart();
        //读取本地文件
        DataHandler dh=new DataHandler(new FileDataSource("src\\main\\resources\\mail.doc"));
        //将附件数据添加到节点
        partAttachment.setDataHandler(dh);
        //设置附件文件名（需要编码）
        partAttachment.setFileName(MimeUtility.encodeText(dh.getName()));

        //设置组合节点与附件的关系
        MimeMultipart multipartMix=new MimeMultipart();
        multipartMix.addBodyPart(partTextPic);
        multipartMix.addBodyPart(partAttachment);
        //混合关系
        multipartMix.setSubType("mixed");

        //设置整个邮件的关系（将最终的混合节点作为邮件内容添加到邮件对象）
        mimeMessage.setContent(multipartMix);
        //设置邮件发送时间,默认立即发送
        mimeMessage.setSentDate(new Date());

        return mimeMessage;
    }

}