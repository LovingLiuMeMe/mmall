<%--
  Created by IntelliJ IDEA.
  User: lovingliu
  Date: 2019-09-08
  Time: 21:00
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>测试上传文件</title>
</head>
<body>
    <h1>用户登陆</h1>
    <form name="form0" action="/user/login.do" method="post">
        <input name="username" value="lovingliu" />
        <input name="password" value="123456" />
        <input type="submit" value="登录"/>
    </form>
    <h1>图片文件上传</h1>
    <form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
        <input type="file" name="upload_file" />
        <input type="submit" value="上传" />
    </form>

    <h1>富文本图片上传文件</h1>
    <form name="form2" action="/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
        <input type="file" name="upload_file" />
        <input type="submit" value="上传" />
    </form>
</body>
</html>
