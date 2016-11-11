package in.andonsystem.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import in.andonsystem.R;
import in.andonsystem.view.LetterImageView;

/**
 * Created by Md Zahid Raza on 17/06/2016.
 */
public class HolderHome extends RecyclerView.ViewHolder {

    LetterImageView icon;
    TextView probName;
    TextView time;
    TextView deptName;
    TextView line;
    TextView issueId;

    public HolderHome(View view){
        super(view);

        icon = (LetterImageView)view.findViewById(R.id.issue_letter_image);
        probName = (TextView)view.findViewById(R.id.issue_prob_name);
        time = (TextView)view.findViewById(R.id.issue_time);
        deptName = (TextView)view.findViewById(R.id.issue_dept_name);
        line = (TextView)view.findViewById(R.id.issue_line);
        issueId = (TextView)view.findViewById(R.id.issue_id);

    }
}
