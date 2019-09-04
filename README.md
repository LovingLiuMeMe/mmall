### 1.git 分支的使用
```git
git branch 查看当前所处分支
git checkout -b v1.0 origin/master 从主分支上检出分支 v1.0 并切换
 git push origin HEAD -u push分支
```

### 2.Spring里PropertyPlaceholderConfigurer类的使用
PropertyPlaceholderConfigurer是个bean工厂后置处理器的实现，也就是 BeanFactoryPostProcessor接口的一个实现。  
PropertyPlaceholderConfigurer可以将上下文（配置文 件）中的属性值放在另一个单独的标准java Properties文件中去。  
在XML文件中用${key}替换指定的properties文件中的值。这样的话，只需要对properties文件进 行修改，而不用对xml配置文件进行修改。
```xml
    <bean id="propertyConfigurer"
          class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="order" value="2"/>
        <property name="ignoreUnresolvablePlaceholders" value="true"/>
        <property name="locations">
            <list>
                <value>classpath:datasource.properties</value>
            </list>
        </property>
        <property name="fileEncoding" value="utf-8"/>
    </bean>
```
为简化`PropertyPlaceholderConfigurer`的使用，Spring提供了`<context:property-placeholder/>`元素。下面给出了配置示例，启用它后，开发者便不用配置PropertyPlaceholderConfigurer对象了。
```xml
<context:property-placeholder location="userinfo.properties"/> 
```
