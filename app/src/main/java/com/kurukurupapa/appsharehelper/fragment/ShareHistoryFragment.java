package com.kurukurupapa.appsharehelper.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.helper.DbHelper;
import com.kurukurupapa.appsharehelper.helper.PreferenceHelper;
import com.kurukurupapa.appsharehelper.helper.ShareHistoryAdapter;
import com.kurukurupapa.appsharehelper.helper.ShareHistoryArrayAdapter;
import com.kurukurupapa.appsharehelper.model.dummy.DummyContent;
import com.kurukurupapa.appsharehelper.service.ShareActivityCacheService;
import com.kurukurupapa.appsharehelper.service.ShareActivityTableService;
import com.kurukurupapa.appsharehelper.service.ShareHistoryService;

import java.util.List;

/**
 * 共有履歴フラグメント
 *
 * 共有履歴データをリスト表示します。
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ShareHistoryFragment extends ListFragment {
    private static final String TAG = ShareHistoryFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private DbHelper mDbHelper;
    private ShareActivityTableService mShareActivityTableService;
    private ShareActivityCacheService mShareActivityCacheService;
    private ShareHistoryService mShareHistoryService;
    private ShareHistoryArrayAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static ShareHistoryFragment newInstance(String param1, String param2) {
        ShareHistoryFragment fragment = new ShareHistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShareHistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // オブジェクト生成
        mDbHelper = new DbHelper(getActivity());
        mShareActivityTableService = new ShareActivityTableService(mDbHelper);
        mShareActivityCacheService = new ShareActivityCacheService(mShareActivityTableService);
        mShareHistoryService = new ShareHistoryService(mDbHelper);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach called");
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * 独自レイアウトでListFragmentを表示します。
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        // 開発者向けスイッチ
        Switch devSwitch = (Switch) view.findViewById(R.id.dev_switch);
        if (PreferenceHelper.getDeveloperFlag(getActivity())) {
            devSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAdapter.setDevFlag(isChecked);
                    mAdapter.notifyDataSetChanged();
                }
            });
        } else {
            devSwitch.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // データなし時TextViewのリンクタップでブラウザ表示されるように設定します。
        TextView emptyView = (TextView) getListView().getEmptyView();
        MovementMethod mMethod = LinkMovementMethod.getInstance();
        emptyView.setMovementMethod(mMethod);
    }

    /**
     * データなしの場合のテキストを設定します。
     * ※独自レイアウトを使用する場合は、オーバーライドする必要があるようです。
     * @param text
     */
    @Override
    public void setEmptyText(CharSequence text) {
        TextView tv = (TextView) getListView().getEmptyView();
        tv.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // 古い共有履歴データを削除
        mShareHistoryService.deleteOld();

        // 共有履歴データを取得
        List<ShareHistoryAdapter> list = mShareHistoryService.find(mShareActivityCacheService, getActivity());

        // 共有履歴データをリストへ紐づけ
        mAdapter = ShareHistoryArrayAdapter.create(getActivity(), list);
        setListAdapter(mAdapter);

        // データなし設定
        CharSequence emptyText;
        if (list.size() == 0) {
            emptyText = Html.fromHtml(getActivity().getString(R.string.msg_share_history_no_items));
        } else {
            emptyText = "";
        }
        setEmptyText(emptyText);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onShareHistoryFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onShareHistoryFragmentInteraction(String id);
    }

}
