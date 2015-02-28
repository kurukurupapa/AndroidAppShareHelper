package com.kurukurupapa.appsharehelper.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.kurukurupapa.appsharehelper.R;
import com.kurukurupapa.appsharehelper.helper.DbHelper;
import com.kurukurupapa.appsharehelper.helper.PreferenceHelper;
import com.kurukurupapa.appsharehelper.helper.StandardAppArrayAdapter;
import com.kurukurupapa.appsharehelper.model.ShareActivity;
import com.kurukurupapa.appsharehelper.model.dummy.DummyContent;
import com.kurukurupapa.appsharehelper.service.ShareActivityService;
import com.kurukurupapa.appsharehelper.service.ShareActivityTableService;

import java.util.List;

/**
 * 標準アプリ一覧フラグメント
 *
 * 標準アプリ設定されている共有アクティビティデータをリスト表示します。
 * <p/>
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class StandardAppFragment extends ListFragment {
    private static final String TAG = StandardAppFragment.class.getSimpleName();

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
    private ShareActivityService mShareActivityService;
    private StandardAppArrayAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static StandardAppFragment newInstance(String param1, String param2) {
        StandardAppFragment fragment = new StandardAppFragment();
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
    public StandardAppFragment() {
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
        mShareActivityService = new ShareActivityService(getActivity(), mShareActivityTableService);
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

        // 共有アクティビティデータを取得します。
        List<ShareActivity> list = mShareActivityService.findStandardApp();

        // 共有アクティビティデータをリストへ紐づけます。
        mAdapter = StandardAppArrayAdapter.create(getActivity(), list,
                new ToggleButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Log.d(TAG, "ToggleButton.onCheckedChanged called");
                        // 標準アプリ設定をDB登録
                        ShareActivity shareActivity = (ShareActivity) buttonView.getTag();
                        shareActivity.setStandardFlag(isChecked);
                        mShareActivityService.update(shareActivity);
                    }
                }
        );
        setListAdapter(mAdapter);

        // データなし設定
        String emptyText;
        if (list.size() == 0) {
            emptyText = getActivity().getString(R.string.msg_standard_app_no_items);
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
            mListener.onStandardAppFragmentInteraction(DummyContent.ITEMS.get(position).id);
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
        public void onStandardAppFragmentInteraction(String id);
    }

}
