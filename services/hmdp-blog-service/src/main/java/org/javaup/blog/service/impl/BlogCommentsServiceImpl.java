package org.javaup.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.javaup.blog.entity.BlogComments;
import org.javaup.blog.mapper.BlogCommentsMapper;
import org.javaup.blog.service.BlogCommentsService;
import org.springframework.stereotype.Service;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements BlogCommentsService {
}
