<template>
    <div>
      <div class="header">
        <span class="title">收到分享</span>
      </div>
      <div class="middle">
        <el-button class="receivebtn" type="primary" icon="el-icon-star-off"
                   @click="QRDecode()">
          扫码接收
        </el-button>
        <el-button class="receivebtn" type="primary" icon="el-icon-star-off"
                   @click="downloadFile()">
          下载文件
        </el-button>

        <!-- 👇 新增：输入链接接收 -->
        <el-input
            v-model="shareLink"
            placeholder="请输入分享链接"
            style="width: 300px; margin-left: 10px;"
            size="small"
        ></el-input>
        <el-button
            type="success"
            icon="el-icon-link"
            size="small"
            style="margin-left: 5px;"
            @click="loadFromShareLink()"
        >
          加载链接
        </el-button>
        <input
            id="file-selector"
            ref="uploadInput"
            type="file"
            @change="uploadFile"
            style="display: none"/>
      </div>
      <div class="line"></div>
      <div class="body">
        <el-table
          :data="fileInfo"
          height="550"
          stripe
          border
          style="width: 100%"
          ref="accountTable"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="150" align="center"> </el-table-column>
          <el-table-column prop="fileName" label="分享文件名" width="300" align="center">
          </el-table-column>
          <el-table-column prop="fileType" label="文件类型" width="150" align="center">
          </el-table-column>
          <el-table-column prop="fileSize" label="文件大小" width="150" align="center">
          </el-table-column>
          <el-table-column prop="ownerName" label="分享人" width="150" align="center">
          </el-table-column>
          <el-table-column prop="downloadTimes" label="下载次数" align="center"> </el-table-column>
        </el-table>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        tableData: "",
        fileList: '',
        fileInfo: [],
        selectedFiles: [],
        // 👇 新增
        shareLink: '' // 用于绑定输入框
      };
    },
    methods: {
      handleRemove(file, fileList) {
        console.log(file, fileList);
      },
      handlePreview(file) {
        console.log(file);
      },
      handleExceed(files, fileList) {
        this.$message.warning(`当前限制选择 3 个文件，本次选择了 ${files.length} 个文件，共选择了 ${files.length + fileList.length} 个文件`);
      },
      beforeRemove(file, fileList) {
        return this.$confirm(`确定移除 ${ file.name }？`);
      },
      saveShare() {

      },
      QRDecode(){//扫码接收
        console.log("扫码接收被点击了");
        this.$refs.uploadInput.click();
      },
      uploadFile(e){
        const file = e.target.files[0];
        console.log("执行了uploadFile方法");
        //获取文件
        let formData = new FormData();
        formData.append('file', file);
        console.log("获取到的文件" + formData);
        //发起请求
        var url = "http://localhost:8989/fileShare/readQRCode";
        let config = {
          headers:{
            'Content-Type':'multipart/form-data',
            'satoken':sessionStorage.getItem("saToken")
          }
        }
        console.log(config);
        this.$http.post(url,formData,config).then(res=>{
          console.log(res);
          let data = res.data;
          //加判断语句
          if(res.data.code!==10000){//若失败，直接提示错误原因
            this.$message.error(res.data.message);
          }else{//成功
            this.$message.success("提取文件" + res.data.message);
            console.log(this.fileInfo);//可以通过this获取到data中的内容
            console.log(this.fileInfo.length);
            this.fileInfo.push(res.data.data.fileInfo);
            console.log(this.fileInfo);
            console.log(this.fileInfo.length);
          }
        })
      },
      // 当用户勾选表格行时触发
      handleSelectionChange(selection) {
        this.selectedFiles = selection;
      },

      // 下载文件按钮点击事件
      downloadFile() {
        if (this.selectedFiles.length === 0) {
          this.$message.warning('请先勾选要下载的文件');
          return;
        }

        let headers={
          'Content-Type': 'multipart/form-data',
          "satoken": sessionStorage.getItem("satoken")
        }

        // 遍历选中的文件，逐个触发浏览器下载
        this.selectedFiles.forEach(file => {
          // 关键：用 window.open 触发原生下载
          console.log(file)
          //window.open(file.downloadUrl, '_blank');
          if(file.downloadTimes > 15) {
            this.$message.warning('文件下载频繁');
            return;
          }
          this.axios.get(file.downloadUrl).then(res => {
            console.log(res)
            this.$message({
              message: "请求成功",
              type: "success"
            })
            file.downloadTimes = (file.downloadTimes || 0) + 1;
          });
        });

      //文件下载
      // downloadFile(){
      //   console.log("下载文件按钮点击");
      //   var that = this;
      //   for (var i=0;i<this.fileInfo.length;i++){
      //     this.$axios.get(that.fileInfo[i].downloadUrl)
      //   }
      // }

      },
      // 👇 新增：从链接加载文件
      loadFromShareLink() {
        if (!this.shareLink.trim()) {
          this.$message.warning('请输入分享链接');
          return;
        }

        // 从 URL 中提取 access_token
        let token = '';
        try {
          const url = new URL(this.shareLink);
          token = url.searchParams.get('access_token');
        } catch (e) {
          this.$message.error('链接格式无效，请输入完整的分享链接');
          return;
        }

        if (!token) {
          this.$message.error('链接中未找到 access_token，请检查链接是否正确');
          return;
        }

        // 调用后端接口获取文件信息（需后端支持）
        const url = `http://localhost:8989/fileShare/getSharedFileInfo?access_token=${token}`;
        const config = {
          headers: {
            'satoken': sessionStorage.getItem("saToken")
          }
        };

        this.$http.get(url, config).then(res => {
          if (res.data.code !== 10000) {
            this.$message.error(res.data.message || '无法加载分享内容');
          } else {
            // 假设返回结构为 res.data.data.fileInfo（与扫码接口一致）
            const fileInfo = res.data.data.fileInfo;
            // 构造 downloadUrl（与扫码返回的一致）
            fileInfo.downloadUrl = `http://localhost:8989/publicDownload?access_token=${token}`;
            // 添加到表格
            this.fileInfo.push(fileInfo);
            this.$message.success(`成功加载文件：${fileInfo.fileName}`);
          }
        }).catch(err => {
          console.error('加载失败', err);
          this.$message.error('网络错误或链接已失效');
        });
      }
    },


  };
  </script>
  
  <style lang="less" scoped>
  .header {
    background-color: #dcdfe6;
    height: 60px;
    line-height: 60px;
    display: flex;
  }
  .title {
    color: #409eff;
    float: left;
    font-size: 30px;
    margin-left: 20px;
  }
  .middle {
    height: 50px;
    line-height: 50px;
    display: flex;
  }
  .receivebtn {
    display: flex;
    margin-left: 10px;
    height: 80%;
    margin-top: 5px;
  }
  .line {
    height: 0;
    width: 100%;
    border: 1px solid #dcdfe6;
  }
  .body {
    margin: 20px 10px;
  }
  </style>