package uchan.weather;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private ListView listView;                      // 리스트뷰
    private boolean lastItemVisibleFlag = false;    // 리스트 스크롤이 마지막 셀(맨 바닥)로 이동했는지 체크할 변수
    private List<String> list;                      // String 데이터를 담고있는 리스트
    private ListViewAdapter adapter;                // 리스트뷰의 아답터
    private int page = 0;                           // 페이징변수. 초기 값은 0 이다.
    private final int OFFSET = 20;                  // 한 페이지마다 로드할 데이터 갯수.
    private ProgressBar progressBar;                // 데이터 로딩중을 표시할 프로그레스바
    private boolean mLockListView = false;          // 데이터 불러올때 중복안되게 하기위한 변수
    static final int MAX_SIZE = 40;
    static final int LENGTH = 30;
    String listTable[] = new String[100];
    int numLength;

    //private List<String> list;          // 데이터를 넣은 리스트변수
    //private ListView listView;          // 검색을 보여줄 리스트변수
    private EditText editSearch;        // 검색어를 입력할 Input 창
    private SearchAdapter searchAdapter;      // 리스트뷰에 연결할 아답터
    private ArrayList<String> searchList;
    private List<String> dataList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_viewpaging);


        editSearch = (EditText) findViewById(R.id.editSearch);
        Intent intent = getIntent();
        numLength = Integer.parseInt(intent.getStringExtra("numLength"));
        Log.d("랭스", String.valueOf(numLength));
        for(int i=0; i<numLength; i++){
            listTable[i] = intent.getStringExtra(String.valueOf(i));
            Log.d("LISTTABLE"+String.valueOf(i),listTable[i]);
        }

        dataList =new ArrayList<String>();
        settingList();
        searchList = new ArrayList<String>();
        searchList.addAll(dataList);

        searchAdapter = new SearchAdapter(dataList,this);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                // input창에 문자를 입력할때마다 호출된다.
                // search 메소드를 호출한다.
                String text = editSearch.getText().toString();
                search(text);
            }
        });
        listView = (ListView) findViewById(R.id.listview);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        list = new ArrayList<String>();
        adapter = new ListViewAdapter(this, list);
        listView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);

        listView.setOnScrollListener(this);

        listView.setOnItemClickListener(itemClickListenerOfLanguageList);

        getItem();
    }
    private AdapterView.OnItemClickListener itemClickListenerOfLanguageList = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> adapterView, View clickedView, int pos, long id)
        {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DIAL);

            intent.setData(Uri.parse("tel:" + "02-2275-9345"));
            //Intent intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:12345"));
            startActivity(intent);
        }
    };




    private void search(String charText) {
        // 문자 입력시마다 리스트를 지우고 새로 뿌려준다.
        list.clear();

        // 문자 입력이 없을때는 모든 데이터를 보여준다.
        if (charText.length() == 0) {
            list.addAll(searchList);
        }
        // 문자 입력을 할때..
        else
        {
            // 리스트의 모든 데이터를 검색한다.
            for(int i = 0;i < searchList.size(); i++)
            {
                // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
                if (searchList.get(i).toLowerCase().contains(charText))
                {
                    // 검색된 데이터를 리스트에 추가한다.
                    list.add(searchList.get(i));
                }
            }
        }
        // 리스트 데이터가 변경되었으므로 아답터를 갱신하여 검색된 데이터를 화면에 보여준다.
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        // 1. OnScrollListener.SCROLL_STATE_IDLE : 스크롤이 이동하지 않을때의 이벤트(즉 스크롤이 멈추었을때).
        // 2. lastItemVisibleFlag : 리스트뷰의 마지막 셀의 끝에 스크롤이 이동했을때.
        // 3. mLockListView == false : 데이터 리스트에 다음 데이터를 불러오는 작업이 끝났을때.
        // 1, 2, 3 모두가 true일때 다음 데이터를 불러온다.
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag && mLockListView == false && (page - 1) * OFFSET <= LENGTH) {
            // 화면이 바닦에 닿을때 처리
            // 로딩중을 알리는 프로그레스바를 보인다.
//            int pageData = (page+1)*OFFSET;
//            Log.d("pageData0",String.valueOf(pageData)); //20
//            Log.d("Length0",String.valueOf(LENGTH));   //30
            progressBar.setVisibility(View.VISIBLE);

            // 다음 데이터를 불러온다.
            getItem();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // firstVisibleItem : 화면에 보이는 첫번째 리스트의 아이템 번호.
        // visibleItemCount : 화면에 보이는 리스트 아이템의 갯수
        // totalItemCount : 리스트 전체의 총 갯수
        // 리스트의 갯수가 0개 이상이고, 화면에 보이는 맨 하단까지의 아이템 갯수가 총 갯수보다 크거나 같을때.. 즉 리스트의 끝일때. true
        lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
    }

    private void getItem() {

        // 리스트에 다음 데이터를 입력할 동안에 이 메소드가 또 호출되지 않도록 mLockListView 를 true로 설정한다.
        mLockListView = true;

//        if(page*OFFSET >= MAX_SIZE)
//            return;
        // 다음 20개의 데이터를 불러와서 리스트에 저장한다.
        int pageData = (page + 1) * OFFSET;

        if ((page + 1) * OFFSET < LENGTH) {
            Log.d("pageData", String.valueOf(pageData)); //20
            Log.d("Length", String.valueOf(LENGTH));   //30
            for (int i = 0; i < 20; i++) {
//                String label = "Label " + ((page * OFFSET) + i);
                list.add(listTable[i]);
            }
        } else {
//            Log.d("pageData", String.valueOf(pageData)); //40
//            Log.d("Length", String.valueOf(LENGTH));   //30
            for (int i = page*OFFSET; i <   numLength; i++) {
//                String label = "Label " + (((page) * OFFSET - OFFSET) + i);
                list.add(listTable[i]);
            }
        }
        // 1초 뒤 프로그레스바를 감추고 데이터를 갱신하고, 중복 로딩 체크하는 Lock을 했던 mLockListView변수를 풀어준다.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                page++;
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                mLockListView = false;
            }
        }, 1000);
    }
    private void settingList(){
      for(int i =0;i<numLength;i++){
          dataList.add(listTable[i]);
      }

    }
}
