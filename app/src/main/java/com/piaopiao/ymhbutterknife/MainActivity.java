package com.piaopiao.ymhbutterknife;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.piaopiao.annotations.BindView;
import com.piaopiao.annotations.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tv)
    TextView textView;

    @BindView(R.id.tv1)
    TextView textView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        YmhButterKnife.binder(this);
        textView.setText("第一个点击事件");
        textView1.setText("第二个点击事件");
    }

    @OnClick({R.id.tv, R.id.tv1})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv:
                Toast.makeText(this, "第一个按钮", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv1:
                Toast.makeText(this, "第二个按钮", Toast.LENGTH_SHORT).show();
                break;
        }

    }

}
