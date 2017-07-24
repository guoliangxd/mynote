package com.guoliang.dnote.activity;

import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.guoliang.dnote.R;
import com.guoliang.dnote.adpter.NoteAdapter;
import com.guoliang.dnote.app.App;
import com.guoliang.dnote.global.FingerprintUtil;
import com.guoliang.dnote.global.NoteFactory;
import com.guoliang.dnote.model.Note;
import com.guoliang.dnote.global.NoteFactory;

import java.util.List;

/**
 * Class ListActivity
 *
 * @author XhinLiang
 */
public class ListActivity extends AppCompatActivity {
    public static final String KEY_EXTRA_NOTE = "note";
    public static final String KEY_EXTRA_PRINTFINGRT_STATE = "printfinger unlock state";

    public static final int REQUEST_FOR_EDIT_NOTE = 100;
    private static final int REQUEST_FOR_CREATE_NOTE = 101;
    public static final int PRINTFINGER_UNLOCK_ENBLE = 1;
    public static final int PRINTFINGER_UNLOCK_CANCLE = 0;

    private NoteAdapter adapter;
    private ListView listView;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;

    private NoteFactory noteFactory = NoteFactory.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initView();
        initData();
        initListView();
        initEvents();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        listView = (ListView) findViewById(R.id.listview_content);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_refresh);
        listView.setEmptyView(findViewById(R.id.empty_view));
        setSupportActionBar(toolbar);


    }

    private void initData() {
        adapter = new NoteAdapter(this, noteFactory.getNotes());
    }

    private void initListView() {
        // 启用 ListView 的嵌套滚动
        listView.setNestedScrollingEnabled(true);
        // 设置 Adapter
        listView.setAdapter(adapter);
        //自动刷新
        refrsh();
    }

    private void initEvents() {
        // ListView 的 Item 点击的时候的逻辑
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra(KEY_EXTRA_NOTE, position);
                startActivityForResult(intent, REQUEST_FOR_EDIT_NOTE);
            }
        });

        //ListView 的 Item 长按时的逻辑
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);

                builder.setMessage(R.string.confirm_delete);
                builder.setTitle(R.string.delete_alert);
                builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // 点击取消时的操作
                        arg0.dismiss();
                    }
                });
                builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // 点击确认时的操作
                        List<Note> notes = noteFactory.getNotes();
                        Note note = notes.get(position);
                        note.deleteInBackground();
                        notes.remove(position);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ListActivity.this, R.string.delete_success,Toast.LENGTH_SHORT).show();
                        arg0.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });

        // 浮动按钮执行的逻辑
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, DetailActivity.class);
                intent.putExtra(KEY_EXTRA_NOTE, -1);
                startActivityForResult(intent, REQUEST_FOR_CREATE_NOTE);
            }
        });

        // 下拉刷新的时候执行的逻辑
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refrsh();

            }
        });

        // ListView 只有在最顶端的时候才可以触发 SwipeRefresh
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (listView == null || listView.getChildCount() == 0) ? 0 : listView.getChildAt(0).getTop();
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		//增加判断条件，使程序只在点击过保存按钮的情况下显示新建/修改成功
        if ((requestCode == REQUEST_FOR_EDIT_NOTE)&&(resultCode == RESULT_OK)) {
            Snackbar.make(fab, R.string.edit_note_success, Snackbar.LENGTH_LONG).show();
            adapter.notifyDataSetChanged();
        }
        if ((requestCode == REQUEST_FOR_CREATE_NOTE)&&(RESULT_OK==resultCode)) {
            Snackbar.make(fab, R.string.create_note_success, Snackbar.LENGTH_LONG)
                    .setAction(R.string.revert, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            noteFactory.getNotes().get(noteFactory.getNotes().size() - 1).deleteInBackground();
                            noteFactory.getNotes().remove(noteFactory.getNotes().size() - 1);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .show();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                AVUser.logOut();
                noteFactory.getNotes().clear();
                finish();
                return true;
            case R.id.action_set_printfinger_unlock:
                SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
                SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                if(pref.getInt(KEY_EXTRA_PRINTFINGRT_STATE,PRINTFINGER_UNLOCK_CANCLE ) == PRINTFINGER_UNLOCK_CANCLE){

                    FingerprintManagerCompat managerCompat = FingerprintManagerCompat.from(App.context);
                    if (!managerCompat.isHardwareDetected()){ //判断设备是否支持
                        Toast.makeText(ListActivity.this,R.string.failed_not_support,Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    KeyguardManager keyguardManager =(KeyguardManager) App.context.getSystemService(App.context.KEYGUARD_SERVICE);
                    if (!keyguardManager.isKeyguardSecure()) {//判断设备是否处于安全保护中
                        Toast.makeText(ListActivity.this, R.string.failed_out_of_protect,Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if (!managerCompat.hasEnrolledFingerprints()){ //判断设备是否已经注册过指纹
                        Toast.makeText(ListActivity.this,R.string.failed_no_singed_fingerprint,Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    editor.putInt(KEY_EXTRA_PRINTFINGRT_STATE,PRINTFINGER_UNLOCK_ENBLE);
                }
                else
                {
                    editor.putInt(KEY_EXTRA_PRINTFINGRT_STATE,PRINTFINGER_UNLOCK_CANCLE);

                }
                editor.apply();
                Toast.makeText(ListActivity.this,"设置成功",Toast.LENGTH_SHORT).show();;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refrsh(){
        AVQuery<AVObject> avQuery = new AVQuery<>("Note");
        avQuery.orderByAscending("createdAt");
        avQuery.whereEqualTo("owner", AVUser.getCurrentUser());
        avQuery.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    Toast.makeText(ListActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
                noteFactory.refresh(list);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
