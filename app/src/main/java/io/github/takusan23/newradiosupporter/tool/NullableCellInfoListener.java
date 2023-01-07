package io.github.takusan23.newradiosupporter.tool;

import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.TelephonyCallback;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

/** NonNull が付いているが、onCellInfoChanged を null で呼び出す事があるため、Nullable にしたもの */
@RequiresApi(api = Build.VERSION_CODES.S)
public interface NullableCellInfoListener extends TelephonyCallback.CellInfoListener {

    @Override
    void onCellInfoChanged(@Nullable List<CellInfo> cellInfo);
}
