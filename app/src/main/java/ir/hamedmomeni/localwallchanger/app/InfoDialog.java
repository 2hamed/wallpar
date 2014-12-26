package ir.hamedmomeni.localwallchanger.app;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

/**
 * Created by hamedpc on 12/26/2014.
 */
public class InfoDialog extends Dialog {
    public InfoDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.info_panel);
    }
}
