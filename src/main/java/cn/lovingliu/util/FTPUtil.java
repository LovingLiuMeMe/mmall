package cn.lovingliu.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @Author：LovingLiu
 * @Description:
 * @Date：Created in 2019-09-08
 */
public class FTPUtil {

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;// vsftpd的一个java客户端

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }



    private static  final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    /**
     * @Desc 构造函数
     * @Author LovingLiu
    */
    public FTPUtil(String ip,int port,String user,String pwd){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }
    /**
     * @Desc 链接Vsftp 服务器
     * @Author LovingLiu
    */
    private boolean connectServer(String ip,int port,String user,String pwd){

        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user,pwd);// 是否登录成功
        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
        }
        return isSuccess;
    }
    /**
     * @Desc 上传文件
     * @Author LovingLiu
    */

    private boolean uploadFile(String remotePath,List<File> fileList) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        //连接FTP服务器
        if(connectServer(this.ip,this.port,this.user,this.pwd)){
            try {
                // 1.设置路径
                ftpClient.changeWorkingDirectory(remotePath);
                // 2.设置缓冲区大小
                ftpClient.setBufferSize(1024);
                // 3.设置编码
                ftpClient.setControlEncoding("UTF-8");
                // 4.设置文件类型为二进制文件类型（避免乱码）
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 5.打开被动模式 因为vsftpd服务器开放的是被动模式的端口范围
                ftpClient.enterLocalPassiveMode();

                for(File fileItem : fileList){
                    fis = new FileInputStream(fileItem);
                    // 存储文件
                    ftpClient.storeFile(fileItem.getName(),fis);
                }

            } catch (IOException e) {
                logger.error("上传文件异常",e);
                uploaded = false;
                e.printStackTrace();
            } finally {
                // 别忘了关闭流和释放资源
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }
    /**
     * @Desc 对外开发出去的方法
     * @Author LovingLiu
    */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始连接ftp服务器");
        boolean result = ftpUtil.uploadFile("images",fileList);// 即/ftpfile/images
        logger.info("开始连接ftp服务器,结束上传,上传结果:{}",result);
        return result;
    }

}
