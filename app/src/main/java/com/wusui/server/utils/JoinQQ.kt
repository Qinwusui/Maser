package com.wusui.server.utils

import android.content.Intent
import android.net.Uri
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.ToastUtils

class JoinQQ {
    fun joinQQGroup(): Boolean {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3DzsYZxi-KuI46tTKh1QDBBUiSWtBEuEsY")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return if (IntentUtils.isIntentAvailable(intent)) {
            ActivityUtils.startActivity(intent)
            true
        } else {
            ToastUtils.showShort("当前未安装QQ或QQ已被冻结！")
            false
        }
    }

    fun joinTestGroup(): Boolean {
        val intent = Intent()
        intent.data =
            Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3DXzVybwlY84Jz8KlEsugkYhPNhf2wi88K")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return if (IntentUtils.isIntentAvailable(intent)) {
            ActivityUtils.startActivity(intent)
            true
        } else {
            ToastUtils.showShort("当前未安装QQ或QQ已被冻结！")
            false
        }
    }
}