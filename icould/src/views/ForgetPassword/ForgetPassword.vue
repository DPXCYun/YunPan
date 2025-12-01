<!-- src/views/ForgetPassword.vue -->
<template>
  <div class="forget-container">
    <h2>找回密码</h2>
    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        style="max-width: 400px; margin: 20px auto;"
    >
      <!-- 邮箱输入 -->
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入注册邮箱"></el-input>
      </el-form-item>

      <!-- 获取验证码按钮 -->
      <el-form-item label="验证码" prop="code">
        <div style="display: flex; gap: 10px;">
          <el-input v-model="form.code" placeholder="请输入验证码" style="flex: 1;"></el-input>
          <el-button
              type="primary"
              :disabled="!canSendCode || isCounting"
              @click="sendCode"
          >
            {{ isCounting ? `${countdown}秒后重发` : '获取验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <!-- 新密码 -->
      <el-form-item label="新密码" prop="newPassword">
        <el-input
            v-model="form.newPassword"
            type="password"
            placeholder="请输入新密码（6-20位）"
            show-password
        ></el-input>
      </el-form-item>

      <!-- 确认密码 -->
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
        ></el-input>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button
            type="primary"
            @click="resetPassword"
            :loading="submitting"
            style="width: 100%;"
        >
          重置密码
        </el-button>
      </el-form-item>
    </el-form>
    <el-button @click="$router.back()">返回登录</el-button>
  </div>
</template>

<script>
export default {
  data() {
    // 自定义校验规则：两次密码一致
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };

    return {
      form: {
        email: '',
        code: '',
        newPassword: '',
        confirmPassword: ''
      },
      rules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        code: [
          { required: true, message: '请输入验证码', trigger: 'blur' },
          { len: 6, message: '验证码为6位数字', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      canSendCode: false,
      isCounting: false,
      countdown: 60,
      submitting: false
    };
  },
  watch: {
    'form.email'(val) {
      this.canSendCode = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
    }
  },
  methods: {
    // 发送验证码
    sendCode() {
      if (!this.canSendCode) {
        this.$message.warning('请输入有效的邮箱');
        return;
      }

      this.isCounting = true;
      this.countdown = 60;

      // 调用后端接口发送验证码
      this.axios({
        method:'POST',
        url:'http://localhost:8989/forgetPassword',
        data:{
          email:this.form.email
        },
        timeout:15000
      }).then(res => {
        if (res.data.success) {
          this.$message.success('验证码已发送，请查收邮箱！');
        } else {
          this.$message.error(res.data.message || '发送失败');
          this.isCounting = false; // 发送失败立即可重试
        }
      }).catch(err => {
        this.$message.error('网络错误，请稍后重试');
        console.error(err);
        this.isCounting = false;
      });

      // 倒计时
      const timer = setInterval(() => {
        if (this.countdown > 0) {
          this.countdown--;
        } else {
          clearInterval(timer);
          this.isCounting = false;
        }
      }, 1000);
    },

    // 重置密码
    resetPassword() {
      this.$refs.formRef.validate(valid => {
        if (!valid) return;

        this.submitting = true;
        this.axios({
            method:'POST',
            url:'http://localhost:8989/resetPassword',
            data:{
              email: this.form.email,
              code: this.form.code,
              newPassword: this.form.newPassword
            }
        }).then(res => {
          if (res.data.success) {
            this.$message.success('密码重置成功！正在跳转登录页...');
            setTimeout(() => {
              this.$router.push('/');
            }, 1500);
          } else {
            this.$message.error(res.data.message || '重置失败');
          }
        }).catch(err => {
          this.$message.error('网络错误，请稍后重试');
          console.error(err);
        }).finally(() => {
          this.submitting = false;
        });
      });
    }
  }
};
</script>

<style scoped>
.forget-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
</style>