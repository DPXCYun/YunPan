package com.example.yunpiyuanpan.controller;

import com.example.yunpiyuanpan.util.R;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class RecyclebinControllerTest {

    @Autowired
    private RecyclebinController recyclebinController;

    @Test
    public void deleteBinFile(){
        try {
            R r = recyclebinController.deleteFile(4l);
            System.out.println(r);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    public void deleteBinFolder(){
        try {
            R r = recyclebinController.deleteFolder(2l);
            System.out.println(r);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getBinFilesAndFolders(){
        try {
            R r = recyclebinController.getFilesByFolderPath("/11/folder/");
            System.out.println(r);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    public void recoverFile(){
        try {
            R r = recyclebinController.recoverFile(1l);
            System.out.println(r);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    @Test
    public void recoverFolder(){
        try {
            R r = recyclebinController.recoverFolder(2l);
            System.out.println(r);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
