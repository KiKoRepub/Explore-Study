package org.deepseek.dynamic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicDataSource extends AbstractRoutingDataSource {
    //备份所有数据源信息，
    private  Map<Object, Object> defineTargetDataSources;

    /**
     * 决定当前线程使用哪个数据源
     */
    @NotNull
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceHolder.getDynamicDataSourceKey();
    }
}
