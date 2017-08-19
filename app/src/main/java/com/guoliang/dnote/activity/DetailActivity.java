package com.guoliang.dnote.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.SaveCallback;
import com.guoliang.dnote.R;
import com.guoliang.dnote.global.NoteFactory;
import com.guoliang.dnote.model.Note;


public class DetailActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private TextView tvContent;
    private EditText etContent;
    private EditText etTitle;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private NoteFactory noteFactory = NoteFactory.getInstance();
    private Note note;
    private boolean editMode = true;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        // ��ʼ��View��ص�����
        initView();
        // ��ʼ������
        initData();
        // ��ʼ���¼�
        initEvent();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        tvContent = (TextView) findViewById(R.id.tv_content);
        etContent = (EditText) findViewById(R.id.et_content);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        // �޸ı����ʱ��������
        etTitle = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        etTitle.setLayoutParams(lp);

        dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.input_new_title)
                .setView(etTitle)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        note.setTitle(etTitle.getText().toString());
                        collapsingToolbarLayout.setTitle(note.getTitle());
                    }
                })
                .create();
    }

    /**
     * ��ʼ������
     */
    private void initData() {
        // ��ô��ݹ�����Intent���ɼ�ListActivity��
        Intent intent = getIntent();
        // �����Ƿ����KEY_EXTRA_NOTE�������
        // ������ˣ������������ȫ�ֵıʼ��б��������ʼǵ�INDEX��Ϊ����������ʾ��һ�εĲ��������һ���Ѿ����ڵıʼ�
        // �������������Ϊ-1����ʾ��һ�εĲ������½�һ���ʼ�
        int noteIndex = intent.getIntExtra(ListActivity.KEY_EXTRA_NOTE, -1);
        // ��������
        if (noteIndex == -1) {
            // �ڱʼǿ������һ���ʼǣ��ʼǱ����������Ĭ�ϵ�
            note = new Note(getString(R.string.edit_title), getString(R.string.edit_content));
            tvContent.setText(note.getContent());
            etContent.setText(note.getContent());
            collapsingToolbarLayout.setTitle(note.getTitle());
            return;
        }
        note = noteFactory.getNotes().get(noteIndex);
        tvContent.setText(note.getContent());
        etContent.setText(note.getContent());
        collapsingToolbarLayout.setTitle(note.getTitle());
    }

    private void initEvent() {
        switchEditMode();

        // ���������¼�
        collapsingToolbarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ������Ǳ༭ģʽ�������κ�����
                if (!editMode) {
                    return;
                }
                // ���Ի������EditText��������Ϊ���ڵı�������
                etTitle.setText(note.getTitle());
                // ��ʾ�Ի���dialog��
                dialog.show();
            }
        });
    }

    /**
     * �л� ���鿴ģʽ�� �� ���༭ģʽ��
     */
    private void switchEditMode() {
        editMode = !editMode;
        // �л���ʾ������״̬
        etContent.setVisibility(editMode ? View.VISIBLE : View.GONE);
        tvContent.setVisibility(editMode ? View.GONE : View.VISIBLE);
        // �л���ť��ͼ��
        fab.setImageResource(editMode ? android.R.drawable.ic_menu_save : android.R.drawable.ic_menu_edit);
        // ����Ǳ༭ģʽ�Ļ��������ť�ȱ���ʼǵı����������ת��ģʽ
        if (editMode) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    note.setContent(etContent.getText().toString());
                    note.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e != null) {
                                Toast.makeText(DetailActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                return;
                            }
                            tvContent.setText(note.getContent());
                            Snackbar.make(fab, R.string.save_success, Snackbar.LENGTH_LONG).show();
                            Intent intent = new Intent(DetailActivity.this,ListActivity.class);
                            setResult(RESULT_OK,intent);
                            switchEditMode();
                        }
                    });
                }
            });
            return;
        }

        // ����ǲ鿴ģʽ�������ťֻ�л�ģʽ
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchEditMode();
            }
        });
    }

    /**
     * ���õ���˵����¼�
     *
     * @param item ����Ĳ˵� Item
     * @return �Ƿ������������¼�
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();

    }
}
