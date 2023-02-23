package io.github.takusan23.newradiosupporter.tool

import android.content.Context
import android.telephony.*
import io.github.takusan23.newradiosupporter.tool.data.FinalNrType
import io.github.takusan23.newradiosupporter.tool.data.NrStandAloneType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * [NetworkStatusFlow.collectNetworkStatus]のテスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NetworkStatusFlowTest {

    @Before
    fun setUp() {
        // mockk を使うと kotlin の object もモックできる
        mockkObject(PermissionCheckTool)
        every { PermissionCheckTool.isGranted(any()) }.returns(true)
    }

    @Test
    fun listenNetworkStatus_5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(643334)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // getSignalStrength もモック
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.NR_SUB6)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun listenNetworkStatus_5Gのミリ波を検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(2070015)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // getSignalStrength もモック
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.NR_MMW)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n257")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 2070015)
    }

    @Test
    fun listenNetworkStatus_転用5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(635424)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // getSignalStrength もモック
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.NR_LTE_FREQUENCY)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 635424)
    }

    @Test
    fun listenNetworkStatus_アンカーバンドを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityLte = mockk<CellIdentityLte>()
        every { cellIdentityLte.earfcn }.returns(1500)
        every { cellIdentityLte.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>() // CellInfoLte なんかモックできないんだけど
        every { cellInfoLte.cellIdentity }.returns(cellIdentityLte)
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)

        // getSignalStrength もモック
        // NR は返さない（アンカーバンドなので）
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(emptyList())

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.ANCHOR_BAND)
        Assert.assertEquals(networkStatusData.bandData.isNR, false)
        Assert.assertEquals(networkStatusData.finalNRType.isNr, false)
        Assert.assertEquals(networkStatusData.bandData.band, "3")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun listenNetworkStatus_もしかして5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityLte = mockk<CellIdentityLte>()
        every { cellIdentityLte.earfcn }.returns(1500)
        every { cellIdentityLte.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>() // CellInfoLte なんかモックできないんだけど
        every { cellInfoLte.cellIdentity }.returns(cellIdentityLte)
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // 5Gの電波強度をモックする
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // CellInfoNr がコールバックで返るように
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.MAYBE_NR)
        Assert.assertEquals(networkStatusData.bandData.isNR, false)
        Assert.assertEquals(networkStatusData.bandData.band, "3")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun listenNetworkStatus_5Gのノンスタンドアローンが検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(643334)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)

        // getSignalStrength もモック
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.NR_SUB6)
        Assert.assertEquals(networkStatusData.nrStandAloneType, NrStandAloneType.NON_STAND_ALONE)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun listenNetworkStatus_5Gのスタンドアローンが検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityNr>()
        every { cellIdentityNr.nrarfcn }.returns(643334)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoNr = mockk<CellInfoNr>()
        every { cellInfoNr.cellIdentity }.returns(cellIdentityNr)
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO)

        // getSignalStrength もモック
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.unregisterTelephonyCallback(any()) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_NR)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.registerTelephonyCallback(any(), any()) }.answers { answers ->
            (answers.invocation.args[1] as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.NR_SUB6)
        Assert.assertEquals(networkStatusData.nrStandAloneType, NrStandAloneType.STAND_ALONE)
        Assert.assertEquals(networkStatusData.bandData.isNR, true)
        Assert.assertEquals(networkStatusData.bandData.band, "n78")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun listenNetworkStatus_アンカーバンドが検出できる_Android11以下() = runTest {
        // Android 11 以下で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(false)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityLte>()
        every { cellIdentityNr.earfcn }.returns(1500)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>()
        every { cellInfoLte.cellIdentity }.returns(cellIdentityNr)
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // TelephonyDisplayInfo をモックする
        val telephonyDisplayInfo = mockk<TelephonyDisplayInfo>()
        every { telephonyDisplayInfo.overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)

        // getSignalStrength もモック
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(emptyList())

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.listen(any(), eq(PhoneStateListener.LISTEN_NONE)) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.listen(any(), any()) }.answers { answers ->
            (answers.invocation.args.first() as PhoneStateListener).onDisplayInfoChanged(telephonyDisplayInfo)
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.ANCHOR_BAND)
        Assert.assertEquals(networkStatusData.bandData.isNR, false)
        Assert.assertEquals(networkStatusData.bandData.band, "3")
        Assert.assertEquals(networkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun listenNetworkStatus_もしかして5Gが検出できる_Android11以下() = runTest {
        // Android 11 以下で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(false)

        // 返り値をモックする
        val cellIdentityNr = mockk<CellIdentityLte>()
        every { cellIdentityNr.earfcn }.returns(1500)
        every { cellIdentityNr.operatorAlphaShort }.returns("DOCOMO")
        val cellInfoLte = mockk<CellInfo>()
        every { cellInfoLte.cellIdentity }.returns(cellIdentityNr)
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // 5Gの電波強度をモックする
        val cellSignalStrengthNr = mockk<CellSignalStrengthNr>()
        val signalStrength = mockk<SignalStrength>()
        every { signalStrength.getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(cellSignalStrengthNr))

        // TelephonyManager をモックしてコールバックの処理を差し替える
        val telephonyManager = mockk<TelephonyManager>()
        every { telephonyManager.networkOperatorName }.returns("DOCOMO")
        every { telephonyManager.createForSubscriptionId(any()) }.returns(telephonyManager)
        every { telephonyManager.listen(any(), eq(PhoneStateListener.LISTEN_NONE)) }.returns(Unit)
        every { telephonyManager.dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
        every { telephonyManager.signalStrength }.returns(signalStrength)
        // コールバックを呼び出して挙動を再現する
        every { telephonyManager.listen(any(), any()) }.answers { answers ->
            (answers.invocation.args.first() as PhoneStateListener).onCellInfoChanged(emptyList())
        }

        // TelephonyManagerを返す
        val context = mockk<Context>()
        every { context.getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
        every { context.getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
        every { context.mainExecutor }.returns(mockk())

        // onCellInfoChanged のあとに onDisplayInfoChanged を呼び出すため、一個目の値は捨てる
        val networkStatusData = NetworkStatusFlow.collectNetworkStatus(context).first()!!
        Assert.assertEquals(networkStatusData.finalNRType, FinalNrType.MAYBE_NR)
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