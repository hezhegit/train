package com.hezhe.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hezhe.train.business.domain.TrainCarriage;
import com.hezhe.train.business.enums.SeatColEnum;
import com.hezhe.train.common.resp.PageResp;
import com.hezhe.train.common.util.SnowUtil;
import com.hezhe.train.business.domain.TrainSeat;
import com.hezhe.train.business.domain.TrainSeatExample;
import com.hezhe.train.business.mapper.TrainSeatMapper;
import com.hezhe.train.business.req.TrainSeatQueryReq;
import com.hezhe.train.business.req.TrainSeatSaveReq;
import com.hezhe.train.business.resp.TrainSeatQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(TrainSeatService.class);

    @Resource
    private TrainSeatMapper trainSeatMapper;

    @Resource
    private TrainCarriageService trainCarriageService;


    public void save(TrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        TrainSeat trainSeat = BeanUtil.copyProperties(req, TrainSeat.class);

        if (ObjectUtil.isNull(req.getId())) {
            // 新增
            trainSeat.setId(SnowUtil.getSnowflakeNextId());
            trainSeat.setCreateTime(now);
            trainSeat.setUpdateTime(now);
            trainSeatMapper.insert(trainSeat);
        }else {
            trainSeat.setUpdateTime(now);
            // 空值 也会更新
            trainSeatMapper.updateByPrimaryKey(trainSeat);
            // 空值 保留 原纪录值
//            trainSeatMapper.updateByPrimaryKeySelective(trainSeat);
        }



    }

    public PageResp<TrainSeatQueryResp> queryList(TrainSeatQueryReq req) {
        TrainSeatExample example = new TrainSeatExample();
        // id 倒序
        example.setOrderByClause("train_code asc, carriage_index asc, carriage_seat_index asc");
        TrainSeatExample.Criteria criteria = example.createCriteria();

        if (ObjectUtil.isNotEmpty(req.getTrainCode())) {
            criteria.andTrainCodeEqualTo(req.getTrainCode());
        }


        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<TrainSeat> trainSeats = trainSeatMapper.selectByExample(example);
        // select count
        PageInfo<TrainSeat> pageInfo = new PageInfo<>(trainSeats);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<TrainSeatQueryResp> trainSeatQueryResps = BeanUtil.copyToList(trainSeats, TrainSeatQueryResp.class);
        PageResp<TrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(trainSeatQueryResps);
        return pageResp;
    }

    public void deleteById(Long id) {
        trainSeatMapper.deleteByPrimaryKey(id);
    }

    // 车厢型号=》一等座or二等座 （这个就可以确定列数了）
    // 一等座 一排 只有 ACDF 四个座位 （4列）， 同理 二等座
    @Transactional
    public void genTrainSeat(String trainCode) {
        DateTime now = DateTime.now();
        // 批量生成：先清空=》再生成
        // 清空当前车次下的所有座位记录
        TrainSeatExample example = new TrainSeatExample();
        TrainSeatExample.Criteria criteria = example.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode);
        trainSeatMapper.deleteByExample(example);

        // 查找当前车次下的所有车厢
        List<TrainCarriage> trainCarriages = trainCarriageService.selectByTrainCode(trainCode);

        LOG.info("当前车次下的车厢数：{}", trainCarriages.size());

        // 循环生成每个车厢的座位
        for (TrainCarriage trainCarriage : trainCarriages) {
            // 拿到车厢数据： 行数， 座位类型(得到列数)
            Integer rowCount = trainCarriage.getRowCount();
            String seatType = trainCarriage.getSeatType();
            int seatIndex = 1;

            // 根据车厢的座位类型，筛选出所有的列，比如车厢类型是一等座 columnList={ACDF}
            List<SeatColEnum> colEnumList = SeatColEnum.getColsByType(seatType);
            LOG.info("根据车厢的座位类型，筛选出所有的列：{}", colEnumList);
            // 循环行数
            for (int row = 1; row <= rowCount; row++) {
                // 循环列数
                for (SeatColEnum seatColEnum : colEnumList) {
                    // 构造座位数据并保存到数据库
                    TrainSeat trainSeat = new TrainSeat();
                    trainSeat.setId(SnowUtil.getSnowflakeNextId());
                    trainSeat.setTrainCode(trainCode);
                    trainSeat.setCarriageIndex(trainCarriage.getIndex());
                    trainSeat.setRow(StrUtil.fillBefore(String.valueOf(row), '0', 2));
                    trainSeat.setCol(seatColEnum.getCode());
                    trainSeat.setSeatType(seatType);
                    trainSeat.setCarriageSeatIndex(seatIndex++);
                    trainSeat.setCreateTime(now);
                    trainSeat.setUpdateTime(now);

                    trainSeatMapper.insert(trainSeat);
                }


            }

        }


    }
}
