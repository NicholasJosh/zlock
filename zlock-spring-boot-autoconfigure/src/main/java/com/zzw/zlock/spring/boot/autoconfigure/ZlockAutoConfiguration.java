/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zzw.zlock.spring.boot.autoconfigure;

import com.zzw.distribution.lock.core.DefaultDistributionLock;
import com.zzw.distribution.lock.core.DistributedLock;
import com.zzw.distribution.lock.core.source.EtcdSource;
import com.zzw.distribution.lock.core.source.RedisSource;
import com.zzw.distribution.lock.core.source.Source;
import com.zzw.distribution.lock.core.source.ZookeeperSource;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.http.HttpClientConnection;
import org.apache.http.client.HttpClient;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.JedisCommands;

import static com.zzw.zlock.spring.boot.constant.ZlockConstant.ZLOCK_PREFIX;

/**
 * zlock auto configuration
 *
 * @author zhaozhiwei
 * @date 2019/9/25 1:12 上午
 */
@Configuration
@ConditionalOnProperty(prefix = ZLOCK_PREFIX, name = "enabled", value = "true", matchIfMissing = true)
@EnableConfigurationProperties(ZlockConfigurationProperties.class)
public class ZlockAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(prefix = ZLOCK_PREFIX, name = "redis")
    @ConditionalOnBean(ZlockConfigurationProperties.class)
    @ConditionalOnClass({JedisCommands.class, JedisPool.class})
    public Source redisSource(ZlockConfigurationProperties properties) {
        RedisConfig redisConfig = properties.getRedis();
        return new RedisSource(redisConfig.getHost(), redisConfig.getPort(), redisConfig.getPassword());
    }

    @Bean
    @ConditionalOnProperty(prefix = ZLOCK_PREFIX, name = "zookeeper")
    @ConditionalOnBean(ZlockConfigurationProperties.class)
    @ConditionalOnMissingBean(Source.class)
    @ConditionalOnClass({ZooKeeper.class, CuratorZookeeperClient.class})
    public Source zookeeperSource(ZlockConfigurationProperties properties) {
        return new ZookeeperSource(properties.getZookeeper().getUrl());
    }

    @Bean
    @ConditionalOnProperty(prefix = ZLOCK_PREFIX, name = "etcd")
    @ConditionalOnBean(ZlockConfigurationProperties.class)
    @ConditionalOnMissingBean(Source.class)
    @ConditionalOnClass({HttpClient.class, HttpClientConnection.class})
    public Source etcdSource(ZlockConfigurationProperties properties) {
        return new EtcdSource(properties.getEtcd().getUrl());
    }

    @Bean
    @ConditionalOnBean(Source.class)
    public DistributedLock distributedLock(Source source) {
        return new DefaultDistributionLock(source);
    }

}
