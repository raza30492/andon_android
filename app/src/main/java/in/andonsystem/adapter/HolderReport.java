package in.andonsystem.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import in.andonsystem.R;
import in.andonsystem.view.LetterImageView;

/**
 * Created by Md Zahid Raza on 26/06/2016.
 */
public class HolderReport extends RecyclerView.ViewHolder{

    LetterImageView icon;
    TextView probName;
    TextView deptName;
    TextView downtime;
    TextView line;

    public HolderReport(View view){
        super(view);

        icon = (LetterImageView) view.findViewById(R.id.report_icon);
        probName = (TextView) view.findViewById(R.id.report_prob_name);
        line = (TextView)view.findViewById(R.id.report_line);
        deptName = (TextView) view.findViewById(R.id.report_dept_name);
        downtime = (TextView) view.findViewById(R.id.report_downtime);
    }


}
