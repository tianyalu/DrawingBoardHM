package com.sty.drawing.board;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageView ivBg;
    private Button btnColorRed;
    private Button btnPainterBold;
    private Button btnReset;
    private Button btnSave;

    private Bitmap srcBitmap;
    private Bitmap copyBitmap;
    private Canvas canvas;
    private Paint paint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();
    }

    private void initViews(){
        //1.找到imageView 以显示画的内容
        ivBg = findViewById(R.id.iv_bg);
        btnColorRed = findViewById(R.id.btn_color_red);
        btnPainterBold = findViewById(R.id.btn_painter_bold);
        btnReset = findViewById(R.id.btn_reset);
        btnSave = findViewById(R.id.btn_save);

        initPaintBoard();
    }

    private void initPaintBoard(){
        //2.把bg转换成bitmap
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg);
        //2.1创建模板
        copyBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
        //2.2 以copyBitmap为模板创建一个画布
        canvas = new Canvas(copyBitmap);
        //2.3 创建一个画笔
        paint = new Paint();
        //2.4 开始作画
        canvas.drawBitmap(srcBitmap, new Matrix(), paint);

        //3.把copyBitmap显示的iv上
        ivBg.setImageBitmap(copyBitmap);
    }

    private void setListeners(){
        btnColorRed.setOnClickListener(this);
        btnPainterBold.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        //4.给iv设置触摸监听事件
        ivBg.setOnTouchListener(new View.OnTouchListener() {
            int startX = 0;
            int startY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //5.获取手指触摸的事件类型
                int action = event.getAction();
                //6.具体判断一下是什么事件类型
                switch (action){
                    case MotionEvent.ACTION_DOWN: //按下
                        //7.获取手指按下坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE: //移动
                        //8.获取手指停止坐标
                        int stopX = (int) event.getX();
                        int stopY = (int) event.getY();

                        //9.画线
                        canvas.drawLine(startX, startY, stopX, stopY, paint);

                        //9.更新一下起点坐标
                        startX = stopX;
                        startY = stopY;

                        //10.更新UI
                        ivBg.setImageBitmap(copyBitmap);
                        break;

                    case MotionEvent.ACTION_UP: //抬起
                        break;
                    default:
                        break;
                }

                return true; //必须返回true，说明消费了事件
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_color_red:
                paint.setColor(Color.RED); //设置画笔颜色
                break;
            case R.id.btn_painter_bold:
                paint.setStrokeWidth(15); //设置画笔宽度
                break;
            case R.id.btn_reset: //重新开始作画
                initPaintBoard();
                break;
            case R.id.btn_save:
                saveImage();
                break;
            default:
                break;
        }
    }

    /**
     * 保存大作
     */
    private void saveImage(){
        try{
            String folderPath = Environment.getExternalStorageDirectory().getPath() + "/sty/";
            File folderFile = new File(folderPath);
            if(!folderFile.exists()){
                folderFile.mkdirs();
            }
            File file = new File(folderFile, "dazuo.png");

            FileOutputStream fos = new FileOutputStream(file);
            //Write a compressed version of the bitmap to the specified outputstream
            copyBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            //关闭流
            fos.close();

            Toast.makeText(this, "大作保存成功", Toast.LENGTH_SHORT).show();

            //发送一条sd卡挂载上来的广播，欺骗一下系统图库应用，使其加载图片
            Intent intent = new Intent();
            //设置action
            intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
            //设置data
            intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            //发送无序广播
            sendBroadcast(intent);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
