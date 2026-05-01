package com.ai.server.service.Impl;

import com.ai.server.beans.Members;
import com.ai.server.mapper.AdminsMapper;
import com.ai.server.service.IMemberService;
import com.ai.server.mapper.MemberMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author bruce
 */
@Service
@Slf4j
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Members> implements IMemberService {
    @Autowired
    AdminsMapper adminsMapper;

}
