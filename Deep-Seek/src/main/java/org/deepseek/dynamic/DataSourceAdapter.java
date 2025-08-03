package org.deepseek.dynamic;

import com.alibaba.druid.pool.DruidDataSource;
import org.deepseek.entity.DataSourceInfo;
import org.deepseek.utils.LoggerUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Map;

//@Component
public class DataSourceAdapter {

    @Resource
    DynamicDataSource dynamicDataSource;

    /**
     * @Description: 根据传递的数据源信息测试数据库连接
     * @Author zhangyu
     */
    public DruidDataSource createDataSourceConnection(DataSourceInfo dataSourceInfo) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dataSourceInfo.getUrl());
        druidDataSource.setUsername(dataSourceInfo.getUserName());
        druidDataSource.setPassword(dataSourceInfo.getPassword());
        druidDataSource.setBreakAfterAcquireFailure(true);
        druidDataSource.setConnectionErrorRetryAttempts(0);
        try {
            druidDataSource.getConnection(2000);
            LoggerUtils.info("数据源连接成功");
            return druidDataSource;
        } catch (SQLException e) {
            LoggerUtils.error(e,"数据源 %s 连接失败,用户名：%s，密码 %s",
                    dataSourceInfo.getUrl(),
                    dataSourceInfo.getUserName(),
                    dataSourceInfo.getPassword());
            return null;
        }
    }

    /**
     * @Description: 将新增的数据源加入到备份数据源map中
     * @Author zhangyu
     */
    public void addDefineDynamicDataSource(DruidDataSource druidDataSource, String dataSourceName){
        Map<Object, Object> defineTargetDataSources = dynamicDataSource.getDefineTargetDataSources();
        defineTargetDataSources.put(dataSourceName, druidDataSource);
        dynamicDataSource.setTargetDataSources(defineTargetDataSources);
        dynamicDataSource.afterPropertiesSet();
    }
}
