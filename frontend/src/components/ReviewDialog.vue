<template>
    <el-dialog :title="title" v-model="dialogVisible" width="500px" @close="handleClose">
        <div class="review-form">
            <el-form ref="reviewForm" :model="reviewForm" :rules="rules" label-width="80px">
                <el-form-item label="评分" prop="score">
                    <el-rate v-model="reviewForm.score" :max="5" :colors="colors" :texts="texts" show-text>
                    </el-rate>
                </el-form-item>
                <el-form-item label="评价" prop="content">
                    <el-input type="textarea" v-model="reviewForm.content" :rows="4" placeholder="请输入您的评价内容，分享您的互助体验">
                    </el-input>
                </el-form-item>
            </el-form>
        </div>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitReview" :loading="submitting">提交评价</el-button>
            </span>
        </template>
    </el-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, toRefs, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { submitReview as submitReviewApi, Review, hasReviewed } from '../api/review';

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
        }
    },
    setup(props, { emit }) {
        const state = reactive({
            dialogVisible: false,
            submitting: false,
            reviewForm: {
                score: 5,
                content: ''
            },
            rules: {
                score: [{ required: true, message: '请选择评分', trigger: 'change' }]
            },
            colors: ['#99A9BF', '#F7BA2A', '#FF9900', '#FF9900', '#67C23A'],
            texts: ['很差', '较差', '一般', '满意', '非常满意']
        });

        // 监听visible属性变化
        const updateVisible = (val: boolean) => {
            state.dialogVisible = val;
            if (val) {
                // 检查用户是否已经评价过
                checkIfReviewed();
            }
        };

        // 检查是否已评价
        const checkIfReviewed = async () => {
            try {
                const hasReviewedResult = await hasReviewed(props.reviewerUserId, props.helpInfoId);
                if (hasReviewedResult) {
                    ElMessage.warning('您已经对该互助进行过评价');
                    handleClose();
                }
            } catch (error) {
                console.error('检查评价状态失败', error);
            }
        };

        // 提交评价
        const submitReview = async () => {
            state.submitting = true;
            try {
                const reviewData: Review = {
                    reviewedUserId: props.reviewedUserId,
                    reviewerUserId: props.reviewerUserId,
                    relatedInfoId: props.helpInfoId,
                    score: state.reviewForm.score,
                    content: state.reviewForm.content
                };

                const result = await submitReviewApi(reviewData);
                if (result) {
                    ElMessage.success('评价提交成功');
                    handleClose();
                    // 重置表单
                    state.reviewForm.score = 5;
                    state.reviewForm.content = '';
                    // 通知父组件评价已提交
                    emit('review-submitted');
                } else {
                    ElMessage.error('评价提交失败');
                }
            } catch (error) {
                console.error('提交评价失败', error);
                ElMessage.error('提交评价失败，请稍后重试');
            } finally {
                state.submitting = false;
            }
        };

        const handleClose = () => {
            state.dialogVisible = false;
            emit('update:visible', false);
        };

        // 监听props变化
        watch(() => props.visible, updateVisible, { immediate: true });

        return {
            ...toRefs(state),
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
