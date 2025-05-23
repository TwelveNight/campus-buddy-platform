<template>
    <el-dialog :title="title" :model-value="dialogVisible" @update:model-value="updateDialog" width="500px"
        @close="handleClose" destroy-on-close>
        <div class="review-form">
            <el-form ref="reviewForm" :model="formData" :rules="rules" label-width="80px">
                <el-form-item label="评分" prop="score">
                    <el-rate v-model="formData.score" :max="5" :colors="colors" :texts="texts" show-text>
                    </el-rate>
                </el-form-item>
                <el-form-item label="评价" prop="content">
                    <el-input type="textarea" v-model="formData.content" :rows="4" placeholder="请输入您的评价内容，分享您的互助体验">
                    </el-input>
                </el-form-item>
            </el-form>
        </div>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="handleClose">取消</el-button>
                <el-button type="primary" @click="submitReview" :loading="submitting">提交评价</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, toRefs, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { submitReview as submitReviewApi, Review, hasReviewed, canReview, getUserReviewStatus } from '../api/review';

export default defineComponent({
    name: 'ReviewDialog',
    props: {
        visible: {
            type: Boolean,
            default: false
        },
        helpInfoId: {
            type: Number,
            required: true
        },
        reviewedUserId: {
            type: Number,
            required: true
        },
        reviewerUserId: {
            type: Number,
            required: true
        },
        title: {
            type: String,
            default: '提交评价'
        },
        reviewType: {
            type: String,
            default: 'PUBLISHER_TO_HELPER' // 或 'HELPER_TO_PUBLISHER'
        }
    },
    setup(props, { emit }) {
        // 使用独立的响应式变量
        const dialogVisible = ref(false);
        const submitting = ref(false);

        // 使用响应式对象存储表单数据
        const formData = reactive({
            score: 5,
            content: ''
        });

        // 表单规则和其他静态数据
        const rules = {
            score: [{ required: true, message: '请选择评分', trigger: 'change' }]
        };
        const colors = ['#99A9BF', '#F7BA2A', '#FF9900', '#FF9900', '#67C23A'];
        const texts = ['很差', '较差', '一般', '满意', '非常满意'];

        // 更新对话框显示状态
        const updateDialog = (val: boolean) => {
            dialogVisible.value = val;
            emit('update:visible', val);
        };

        // 监听visible属性变化
        const updateVisible = (val: boolean) => {
            dialogVisible.value = val;
            if (val) {
                // 重置表单数据
                formData.score = 5;
                formData.content = '';
                // 检查用户是否已经评价过
                checkIfReviewed();
            }
        };

        // 检查是否可以评价（已包含了评价类型判断）
        const checkIfReviewed = async () => {
            try {
                // 使用完整的评价状态API获取用户评价状态
                const statusRes = await getUserReviewStatus(props.reviewerUserId, props.helpInfoId);

                // 根据评价类型检查是否可以评价
                let canReviewFlag = false;
                let hasReviewedFlag = false;

                if (statusRes && statusRes.data) {
                    if (props.reviewType === 'PUBLISHER_TO_HELPER') {
                        canReviewFlag = statusRes.data.canPublisherReview;
                        hasReviewedFlag = statusRes.data.publisherHasReviewed;
                    } else if (props.reviewType === 'HELPER_TO_PUBLISHER') {
                        canReviewFlag = statusRes.data.canHelperReview;
                        hasReviewedFlag = statusRes.data.helperHasReviewed;
                    }
                }

                if (!canReviewFlag || hasReviewedFlag) {
                    ElMessage.warning('您已经评价过或当前无法对该互助进行评价');
                    handleClose();
                    return;
                }

            } catch (error) {
                // 备用方案：使用原有的canReview API
                try {
                    const canReviewResult = await canReview(
                        props.reviewerUserId,
                        props.helpInfoId,
                        props.reviewType
                    );

                    if (!canReviewResult) {
                        ElMessage.warning('您已经评价过或当前无法对该互助进行评价');
                        handleClose();
                        return;
                    }
                } catch (fallbackError) {
                    handleClose();
                }
            }
        };

        // 提交评价
        const submitReview = async () => {
            submitting.value = true;
            try {
                // 再次检查是否可以评价，使用完整的评价状态API
                const statusRes = await getUserReviewStatus(props.reviewerUserId, props.helpInfoId);

                // 根据评价类型检查是否可以评价
                let canReviewFlag = false;
                let hasReviewedFlag = false;

                if (statusRes && statusRes.data) {
                    if (props.reviewType === 'PUBLISHER_TO_HELPER') {
                        canReviewFlag = statusRes.data.canPublisherReview;
                        hasReviewedFlag = statusRes.data.publisherHasReviewed;
                    } else if (props.reviewType === 'HELPER_TO_PUBLISHER') {
                        canReviewFlag = statusRes.data.canHelperReview;
                        hasReviewedFlag = statusRes.data.helperHasReviewed;
                    }
                }

                if (!canReviewFlag || hasReviewedFlag) {
                    ElMessage.warning('您已经评价过或当前无法对该互助进行评价');
                    handleClose();
                    submitting.value = false;
                    return;
                }

                // 全面检查必要字段
                if (!props.reviewedUserId) {
                    ElMessage.error('缺少被评价用户信息，无法提交评价');
                    submitting.value = false;
                    return;
                }

                if (!props.reviewerUserId) {
                    ElMessage.error('缺少评价者信息，无法提交评价');
                    submitting.value = false;
                    return;
                }

                if (!props.helpInfoId) {
                    ElMessage.error('缺少互助信息，无法提交评价');
                    submitting.value = false;
                    return;
                }

                if (!formData.score) {
                    ElMessage.error('请选择评分');
                    submitting.value = false;
                    return;
                }

                const reviewData: Review = {
                    reviewedUserId: props.reviewedUserId,
                    reviewerUserId: props.reviewerUserId,
                    relatedInfoId: props.helpInfoId,
                    score: formData.score,
                    content: formData.content || '',  // 确保不为undefined
                    reviewType: props.reviewType,
                    moduleType: 'HELP' // 设置固定值，确保数据完整
                };

                const result = await submitReviewApi(reviewData);

                if (result && result.data) {
                    if (result.status === 200 || result.data.success === true) {
                        ElMessage.success('评价提交成功');
                        handleClose();
                        formData.score = 5;
                        formData.content = '';
                        emit('review-submitted');
                    } else {
                        ElMessage.error(result.data.message || '评价提交失败，请检查必填字段');
                    }
                } else {
                    ElMessage.error('评价提交失败，服务器无响应');
                }
            } catch (error: any) {
                const errorMsg = error.response ?
                    `提交评价失败: ${error.response.status} ${error.response.statusText}` :
                    '提交评价失败，请稍后重试';
                ElMessage.error(errorMsg);
            } finally {
                submitting.value = false;
            }
        };

        const handleClose = () => {
            dialogVisible.value = false;
            emit('update:visible', false);
        };

        // 监听props变化
        watch(() => props.visible, updateVisible, { immediate: true });

        // 初始设置对话框状态
        onMounted(() => {
            dialogVisible.value = props.visible;
        });

        return {
            dialogVisible,
            submitting,
            formData,
            rules,
            colors,
            texts,
            updateDialog,
            submitReview,
            handleClose
        };
    }
});
</script>

<style scoped>
.review-form {
    padding: 10px 0;
}
</style>
