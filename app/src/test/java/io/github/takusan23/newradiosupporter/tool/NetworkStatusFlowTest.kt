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
 * [NetworkStatusFlow.collectMultipleNetworkStatus]のテスト
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
    fun collectMultipleNetworkStatus_5GのSub6を検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { unregisterTelephonyCallback(any()) }.returns(Unit)
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))
            })
            // コールバックを呼び出して挙動を再現する
            every { registerTelephonyCallback(any(), any()) }.answers { call ->
                (call.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
            }
        }

        // 返り値をモックする
        val cellInfoNr = mockk<CellInfoNr>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityNr>().apply {
                every { nrarfcn }.returns(643334)
                every { operatorAlphaShort }.returns("docomo")
                every { bands }.returns(intArrayOf())
            })
        }
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.NR_SUB6)
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, true)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "n78")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun collectMultipleNetworkStatus_5Gのミリ波を検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { unregisterTelephonyCallback(any()) }.returns(Unit)
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))
            })
            // コールバックを呼び出して挙動を再現する
            every { registerTelephonyCallback(any(), any()) }.answers { call ->
                (call.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
            }
        }

        val cellInfoNr = mockk<CellInfoNr>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityNr>().apply {
                every { nrarfcn }.returns(2070015)
                every { operatorAlphaShort }.returns("docomo")
                every { bands }.returns(intArrayOf())
            })
        }
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.NR_MMW)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, true)
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "n257")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 2070015)
    }

    @Test
    fun collectMultipleNetworkStatus_5Gの転用5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { unregisterTelephonyCallback(any()) }.returns(Unit)
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))
            })
            // コールバックを呼び出して挙動を再現する
            every { registerTelephonyCallback(any(), any()) }.answers { call ->
                (call.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
            }
        }

        val cellInfoNr = mockk<CellInfoNr>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityNr>().apply {
                every { nrarfcn }.returns(157690)
                every { operatorAlphaShort }.returns("docomo")
                every { bands }.returns(intArrayOf())
            })
        }
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.NR_LTE_FREQUENCY)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, true)
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType.isNr, true)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "n28")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 157690)
    }

    @Test
    fun collectMultipleNetworkStatus_アンカーバンドを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { unregisterTelephonyCallback(any()) }.returns(Unit)
            // getSignalStrength もモック
            // NR は返さない（アンカーバンドなので）
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(emptyList())
            })
            // コールバックを呼び出して挙動を再現する
            every { registerTelephonyCallback(any(), any()) }.answers { call ->
                // TelephonyDisplayInfo をモックする
                (call.invocation.args[1] as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(mockk<TelephonyDisplayInfo>().apply {
                    every { overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)
                })
            }
        }

        // アンカーバンドだと LTE なので
        val cellInfoLte = mockk<CellInfo>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityLte>().apply {
                every { earfcn }.returns(1500)
                every { operatorAlphaShort }.returns("docomo")
            })
        }
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.ANCHOR_BAND)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, false)
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType.isNr, false)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "3")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun collectMultipleNetworkStatus_もしかして5Gを検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { unregisterTelephonyCallback(any()) }.returns(Unit)
            // 5Gの電波強度をモックする
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))
            })
            // コールバックを呼び出して挙動を再現する
            every { registerTelephonyCallback(any(), any()) }.answers { call ->
                (call.invocation.args[1] as TelephonyCallback.CellInfoListener).onCellInfoChanged(emptyList())
            }
        }

        // アンカーバンドだと LTE なので
        val cellInfoLte = mockk<CellInfo>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityLte>().apply {
                every { earfcn }.returns(1500)
                every { operatorAlphaShort }.returns("docomo")
            })
        }
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.MAYBE_NR)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, false)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "3")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun collectMultipleNetworkStatus_5Gのノンスタンドアローンが検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { unregisterTelephonyCallback(any()) }.returns(Unit)
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))
            })
            // コールバックを呼び出して挙動を再現する
            every { registerTelephonyCallback(any(), any()) }.answers { call ->
                (call.invocation.args[1] as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(mockk<TelephonyDisplayInfo>().apply {
                    every { overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)
                })
            }
        }

        // 返り値をモックする
        val cellInfoNr = mockk<CellInfoNr>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityNr>().apply {
                every { nrarfcn }.returns(643334)
                every { operatorAlphaShort }.returns("docomo")
                every { bands }.returns(intArrayOf())
            })
        }
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.NR_SUB6)
        Assert.assertEquals(firstSimNetworkStatusData.nrStandAloneType, NrStandAloneType.NON_STAND_ALONE)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, true)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "n78")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 643334)
    }

    @Test
    fun collectMultipleNetworkStatus_5Gのスタンドアローンが検出できる() = runTest {
        // Android 12 以上で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(true)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_NR)
            every { createForSubscriptionId(any()) }.returns(this)
            every { unregisterTelephonyCallback(any()) }.returns(Unit)
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))
            })
            // コールバックを呼び出して挙動を再現する
            every { registerTelephonyCallback(any(), any()) }.answers { call ->
                (call.invocation.args[1] as TelephonyCallback.DisplayInfoListener).onDisplayInfoChanged(mockk<TelephonyDisplayInfo>().apply {
                    every { overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED)
                })
            }
        }

        // 返り値をモックする
        val cellInfoNr = mockk<CellInfoNr>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityNr>().apply {
                every { nrarfcn }.returns(703392)
                every { operatorAlphaShort }.returns("docomo")
                every { bands }.returns(intArrayOf())
            })
        }
        // Qualcomm Snapdragon だと CellInfoNr 以外に CellInfoLte が入ってたりするので
        val cellInfoLte = mockk<CellInfoLte>()
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte, cellInfoNr))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.NR_SUB6)
        Assert.assertEquals(firstSimNetworkStatusData.nrStandAloneType, NrStandAloneType.STAND_ALONE)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, true)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "n79")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 703392)
    }

    @Test
    fun collectMultipleNetworkStatus_アンカーバンドが検出できる_Android11以下() = runTest {
        // Android 11 以下で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(false)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { listen(any(), eq(PhoneStateListener.LISTEN_NONE)) }.returns(Unit)
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(emptyList())
            })
            // コールバックを呼び出して挙動を再現する
            every { listen(any(), any()) }.answers { call ->
                (call.invocation.args.first() as PhoneStateListener).onDisplayInfoChanged(mockk<TelephonyDisplayInfo>().apply {
                    every { overrideNetworkType }.returns(TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA)
                })
            }
        }

        // 返り値をモックする
        val cellInfoLte = mockk<CellInfo>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityLte>().apply {
                every { earfcn }.returns(1500)
                every { operatorAlphaShort }.returns("docomo")
            })
        }
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.ANCHOR_BAND)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, false)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "3")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 1500)
    }

    @Test
    fun collectMultipleNetworkStatus_もしかして5Gが検出できる_Android11以下() = runTest {
        // Android 11 以下で
        mockkObject(NetworkStatusFlow)
        every { NetworkStatusFlow.getProperty("isAndroidSAndLater") }.returns(false)

        // UnitTest で使えない Android の関数を全部モックしていく

        val telephonyManager = mockk<TelephonyManager>().apply {
            every { networkOperatorName }.returns("docomo")
            every { dataNetworkType }.returns(TelephonyManager.NETWORK_TYPE_LTE)
            every { createForSubscriptionId(any()) }.returns(this)
            every { listen(any(), eq(PhoneStateListener.LISTEN_NONE)) }.returns(Unit)
            // 5Gの電波強度をモックする
            every { signalStrength }.returns(mockk<SignalStrength>().apply {
                every { getCellSignalStrengths<CellSignalStrengthNr>(any()) }.returns(listOf(mockk()))
            })
            // コールバックを呼び出して挙動を再現する
            every { listen(any(), any()) }.answers { call ->
                (call.invocation.args.first() as PhoneStateListener).onCellInfoChanged(emptyList())
            }
        }

        // 返り値をモックする
        val cellInfoLte = mockk<CellInfo>().apply {
            every { cellIdentity }.returns(mockk<CellIdentityLte>().apply {
                every { earfcn }.returns(1500)
                every { operatorAlphaShort }.returns("docomo")
            })
        }
        // waitRequestCellInfoUpdate も適当にモックする
        coEvery { NetworkStatusFlow.invoke("waitRequestCellInfoUpdate").withArguments(listOf(ofType(Context::class), ofType(TelephonyManager::class))) }.returns(listOf(cellInfoLte))

        // Context#getSystemService をモック
        val context = mockk<Context>().apply {
            every { getSystemService(eq(Context.TELEPHONY_SERVICE)) }.returns(telephonyManager)
            every { getSystemService(eq(Context.TELEPHONY_SUBSCRIPTION_SERVICE)) }.returns(createMockSubscriptionManager())
            every { mainExecutor }.returns(mockk())
        }

        val (firstSimNetworkStatusData, _) = NetworkStatusFlow.collectMultipleNetworkStatus(context).first()
        Assert.assertEquals(firstSimNetworkStatusData.finalNRType, FinalNrType.MAYBE_NR)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.isNR, false)
        Assert.assertEquals(firstSimNetworkStatusData.bandData.band, "3")
        Assert.assertEquals(firstSimNetworkStatusData.bandData.earfcn, 1500)
    }

    /** [SubscriptionManager]をモックする */
    private fun createMockSubscriptionManager(): SubscriptionManager = mockk<SubscriptionManager>().apply {
        // モックしたものを返す
        every { getActiveSubscriptionInfo(any()) }.returns(mockk<SubscriptionInfo>().apply {
            every { simSlotIndex }.returns(0)
        })
        every { activeSubscriptionInfoList }.returns((0 until 2).map { index ->
            mockk<SubscriptionInfo>().apply {
                every { subscriptionId }.returns(index)
            }
        })
        // コールバックを呼び出す
        every { addOnSubscriptionsChangedListener(any(), any()) }.answers { call ->
            (call.invocation.args[1] as SubscriptionManager.OnSubscriptionsChangedListener).onSubscriptionsChanged()
        }
        every { removeOnSubscriptionsChangedListener(any()) }.returns(Unit)
    }
}