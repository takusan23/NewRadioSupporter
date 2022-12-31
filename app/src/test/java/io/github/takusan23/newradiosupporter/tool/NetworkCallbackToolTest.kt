package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.telephony.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * 5G判定関数のテスト [NetworkCallbackTool.listenNetworkStatus]のテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkCallbackToolTest {

    @Before
    fun setUp() {
        // mockk を使うと kotlin の object もモックできる
        mockkObject(PermissionCheckTool)
        every { PermissionCheckTool.isGranted(any()) }.returns(true)
    }

    @Test
    fun listenNetworkStatus_5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(643334)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(listOf(cellInfoLte, cellInfoNr))
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.NR_SUB6)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun listenNetworkStatus_5Gのミリ波を検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(2070015)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(listOf(cellInfoLte, cellInfoNr))
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.NR_MMW)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n257")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 2070015)
    }

    @Test
    fun listenNetworkStatus_転用5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(635424)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(listOf(cellInfoLte, cellInfoNr))
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.NR_LTE_FREQUENCY)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 635424)
    }

    @Test
    fun listenNetworkStatus_アンカーバンドを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityLte = mockk<CellIdentityLte>()
        every { cellIdentityLte.earfcn }.returns(1500)
        every { cellIdentityLte.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>() // CellInfoLte なんかモックできないんだけど
        every { cellInfoLte.cellIdentity }.returns(cellIdentityLte)

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(listOf(cellInfoLte))
            (answers.invocation.args[1] as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).drop(1).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.ANCHOR_BAND)
        Assert.assertEquals(networkStatusData.bandData.isNR, false)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, false)
        Assert.assertEquals(networkStatusData.bandData.band, "3")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun listenNetworkStatus_もしかして5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityLte = mockk<CellIdentityLte>()
        every { cellIdentityLte.earfcn }.returns(1500)
        every { cellIdentityLte.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>() // CellInfoLte なんかモックできないんだけど
        every { cellInfoLte.cellIdentity }.returns(cellIdentityLte)

        // 5Gの電波強度をモックする
        val cellSignalStrengthNr = mockk<CellSignalStrengthNr>()
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(cellSignalStrengthNr))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(listOf(cellInfoLte))
            (answers.invocation.args[1] as TelephonyCallback.SignalStrengthsListener).onSignalStrengthsChanged(signalStrength)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).drop(1).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.MAYBE_NR)
        Assert.assertEquals(networkStatusData.bandData.isNR, false)
        Assert.assertEquals(networkStatusData.bandData.band, "3")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun listenNetworkStatus_5Gのノンスタンドアローンが検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(643334)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            val callback = answers.invocation.args[1]
            (callback as TelephonyCallback.CellInfoListener).onCellInfoChanged(listOf(cellInfoLte, cellInfoNr))
            (callback as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).drop(1).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.NR_SUB6)
        Assert.assertEquals(networkStatusData.nrStandAloneType, NrStandAloneType.NON_STAND_ALONE)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun listenNetworkStatus_5Gのスタンドアローンが検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(643334)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO)

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_NR)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            val callback = answers.invocation.args[1]
            (callback as TelephonyCallback.CellInfoListener).onCellInfoChanged(listOf(cellInfoLte, cellInfoNr))
            (callback as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).drop(1).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.NR_SUB6)
        Assert.assertEquals(networkStatusData.nrStandAloneType, NrStandAloneType.STAND_ALONE)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun listenNetworkStatus_アンカーバンドが検出できる_Android11以下() = runTest {
        // Android 11 以下で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(false)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityLte>()
        every { cellIdentityNr.earfcn }.returns(1500)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>()
        every { cellInfoLte.cellIdentity }.returns(cellIdentityNr)

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.listen(any(), eq(PhoneStateListener.LISTEN_NONE)) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.listen(any(), any()) }.answers { answers ->
            val callback = answers.invocation.args.first() as PhoneStateListener
            callback.onCellInfoChanged(listOf(cellInfoLte))
            callback.onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).drop(1).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.ANCHOR_BAND)
        Assert.assertEquals(networkStatusData.bandData.isNR, false)
        Assert.assertEquals(networkStatusData.bandData.band, "3")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun listenNetworkStatus_もしかして5Gが検出できる_Android11以下() = runTest {
        // Android 11 以下で
        mockkObject(NetworkCallbackTool)
        every { NetworkCallbackTool.getProperty("isAndroidSAndLater") }.returns(false)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityLte>()
        every { cellIdentityNr.earfcn }.returns(1500)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>()
        every { cellInfoLte.cellIdentity }.returns(cellIdentityNr)

        // 5Gの電波強度をモックする
        val cellSignalStrengthNr = mockk<CellSignalStrengthNr>()
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(cellSignalStrengthNr))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.listen(any(), eq(PhoneStateListener.LISTEN_NONE)) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.listen(any(), any()) }.answers { answers ->
            val callback = answers.invocation.args.first() as PhoneStateListener
            callback.onCellInfoChanged(listOf(cellInfoLte))
            callback.onSignalStrengthsChanged(signalStrength)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkCallbackTool.listenNetworkStatus(context).drop(1).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNRType.MAYBE_NR)
        Assert.assertEquals(networkStatusData.bandData.isNR, false)
        Assert.assertEquals(networkStatusData.bandData.band, "3")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 1500)
    }

    /** [SubscriptionManager]をモックする */
    private fun createMockSubscriptionManager(): SubscriptionManager {
        val subscriptionInfo = mockk<SubscriptionInfo>()
        every { subscriptionInfo.simSlotIndex }.returns(0)
        val subscriptionManager = mockk<SubscriptionManager>()
        every { subscriptionManager.getActiveSubscriptionInfo(any()) }.returns(subscriptionInfo)

        return subscriptionManager
    }

}