package com.rentwise.training.service;

import com.rentwise.training.domain.*;
import com.rentwise.training.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TrainingDemoDataInitializer implements ApplicationRunner {
    private final TrainingCaseRepository caseRepository;
    private final LearningCardRepository cardRepository;

    public TrainingDemoDataInitializer(TrainingCaseRepository caseRepository, LearningCardRepository cardRepository) {
        this.caseRepository = caseRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (caseRepository.count() == 0) seedCases();
        if (cardRepository.count() == 0) seedCards();
    }

    private void seedCases() {
        add(RiskTopic.DEPOSIT_RETURN, Difficulty.EASY, "押金在退租后统一结算，具体扣除项目以出租方最终核算为准。", true,
                "扣除项目和依据没有写清，签约前应确认什么情况下能扣、由谁举证以及何时返还。", "押金最晚什么时候返还？哪些项目可以扣除，需要提供什么凭证？");
        add(RiskTopic.DEPOSIT_RETURN, Difficulty.MEDIUM, "承租人无欠费且房屋验收无异常的，押金应在约定期限内返还。", false,
                "条款已经给出了返还条件和期限框架，重点是确认合同中是否写明具体期限。", "合同里约定的具体返还天数是多少？");
        add(RiskTopic.EARLY_TERMINATION, Difficulty.EASY, "承租人提前退租时，无论原因均需支付剩余全部租期租金。", true,
                "责任范围过于绝对，应确认通知期限、违约金计算方式以及特殊情况如何处理。", "提前退租的违约责任如何计算？是否存在可以协商或免责的情形？");
        add(RiskTopic.EARLY_TERMINATION, Difficulty.MEDIUM, "提前退租需提前三十日通知，并按合同约定承担违约责任。", false,
                "条款说明了通知要求并指向具体违约约定，需要继续核对对应条款是否清楚。", "违约责任对应合同的哪一条，具体金额或计算方式是什么？");
        add(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.EASY, "租赁期间房屋内所有设施维修费用均由承租人承担。", true,
                "把所有维修责任统一转给承租人会模糊自然损耗与人为损坏的边界。", "自然损耗、设备老化和人为损坏分别由谁承担维修费用？");
        add(RiskTopic.REPAIR_RESPONSIBILITY, Difficulty.MEDIUM, "因承租人使用不当造成的损坏由承租人负责维修。", false,
                "条款限定在承租人使用不当造成的损坏，责任边界相对明确。", "发生争议时，如何认定损坏是否属于使用不当？");
        add(RiskTopic.COST_BEARING, Difficulty.EASY, "租赁期间产生的其他费用由承租人承担，费用范围另行确定。", true,
                "“其他费用”范围不明确，容易产生签约后新增收费的不确定性。", "除房租和水电外，还有哪些费用？每项费用的标准和收款方是谁？");
        add(RiskTopic.COST_BEARING, Difficulty.MEDIUM, "水电燃气按实际使用量及公开标准由承租人承担。", false,
                "费用项目和计费原则已经说明，仍可在签约时核对具体表计和缴费方式。", "水电燃气按什么表计结算，由谁代收还是直接缴费？");
        add(RiskTopic.BREACH_LIABILITY, Difficulty.EASY, "承租人发生任何违约行为均需支付三个月租金作为违约金。", true,
                "没有区分违约行为和后果，统一固定责任值得进一步确认其适用范围和计算依据。", "哪些行为会触发这项违约金？出租方违约时承担什么责任？");
        add(RiskTopic.BREACH_LIABILITY, Difficulty.MEDIUM, "双方违约责任、计算方式与处理流程按本合同具体约定执行。", false,
                "该条款本身是索引性表达，应继续核对合同中的具体责任条款是否完整、对等。", "双方各自的违约责任分别在哪些条款中约定？");
    }

    private void add(RiskTopic topic, Difficulty difficulty, String clause, boolean shouldClarify,
                     String explanation, String followUp) {
        caseRepository.save(new TrainingCase(topic, difficulty, clause, "这条内容是否值得进一步确认？",
                shouldClarify, true, explanation, followUp));
    }

    private void seedCards() {
        cardRepository.save(new LearningCard(RiskTopic.DEPOSIT_RETURN, "押金返还识别卡", "确认返还时间、可扣除项目以及扣款依据是否写清楚。"));
        cardRepository.save(new LearningCard(RiskTopic.EARLY_TERMINATION, "提前退租识别卡", "关注通知期限、违约金计算方式，以及特殊情况下的处理方式。"));
        cardRepository.save(new LearningCard(RiskTopic.REPAIR_RESPONSIBILITY, "维修责任识别卡", "区分自然损耗与人为损坏，确认房东和租客各自承担的维修范围。"));
        cardRepository.save(new LearningCard(RiskTopic.COST_BEARING, "费用承担识别卡", "确认费用名称、计费标准、缴费对象和是否存在未列明费用。"));
        cardRepository.save(new LearningCard(RiskTopic.BREACH_LIABILITY, "违约责任识别卡", "确认什么行为构成违约、责任是否对等、违约金如何计算。"));
    }
}
