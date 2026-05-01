package com.ai.server.service.Impl;

import com.ai.server.beans.News;
import com.ai.server.mapper.NewsMapper;
import com.ai.server.service.INewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 资讯表 服务实现类
 * </p>
 * @author bruce
 */
@Service
public class NewsServiceImpl extends ServiceImpl<NewsMapper, News> implements INewsService {

}
