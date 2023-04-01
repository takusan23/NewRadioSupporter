# NewRadioSupporter

アンテナピクト📶の隣に居座る5Gアイコンが何を示しているのか、その真実に迫るアプリです。

<p align="center">
<img width="200" src="https://imgur.com/CzCjQSn.jpg">
<img width="200" src="https://imgur.com/0mVypsW.jpg">
<img width="200" src="https://imgur.com/porNfMv.jpg">
<img width="200" src="https://imgur.com/HxdIGzT.jpg">
</p>

<p align="center">
<img width="200" src="https://imgur.com/Gy099KS.png">
<img width="200" src="https://imgur.com/QSx4MAQ.png">
<img width="200" src="https://imgur.com/S9x7ck0.png">
<img width="200" src="https://imgur.com/ypkZRDn.png">
</p>

# ダウンロード
https://play.google.com/store/apps/details?id=io.github.takusan23.newradiosupporter

# 機能
- Sub-6 / ミリ波 / アンカーバンド の検出
- 5Gの場合は スタンドアローン方式 / ノンスタンドアローン方式 の表示
- デュアルSIM 対応
  - 多分 5G はデータ通信に設定したSIMカードしか拾わない気がします
- ウィジェットを追加してホーム画面から確認
- バックグラウンドでも通知領域から確認
- おまけ程度のバンド表示
  - `n257`とか

(なんか従量制ネットワークか無制限ネットワークか検出出来るよ！って書いてあったんだけど検出できてない。流石にできないやろ...)

# バンド表示
`MCC / MNC`が日本のキャリアの場合は追加の処理を行います。  
これは`NR-ARFCN`が複数のバンドに一致する場合、通信キャリアが提供しているバンドを優先して返すようにするためです。  
（`NR-ARFCN`が`643334`だと`n48 n77 n78`のどれかになるらしい。対応表から探すと多分一番最初の`n48`になるけど、日本で`n48`使ってるところはないので`n77 n78`のどちらかになるはず。）

![Imgur](https://imgur.com/S6JMkun.png)

（提供しているバンドはソースコードに書きました、負けた気分）

本当は`CellIdentityNr#getBands`っていうバンドを返す関数があるのですが、この関数自体が`モデムから報告された値`ではなく、`NR-ARFCN とバンドの対応表`から探してきたものらしい（？）、  
ので結局複数のバンドに一致する場合に一つに絞れない。  

Pixel 端末はモデムから取得するよう修正されたらしいけど、その他の端末はわからん。  
https://issuetracker.google.com/issues/253539094

# 開発者向け
JetpackComposeでできてます。最新のAndroid Studioで実行できるはずです。

## superuserブランチ
`Shizuku API`を利用し、`PhysicalChannelConfig`から電波状況を取得してみたものです。
5Gのプライマリ、セカンダリセルの内訳 や 4Gのキャリアアグリゲーションの内訳 が見れるらしいです。  
上記のAPIはサードパーティーには公開されていないので、`Shizuku API ( adb or root )`が必要です。

また、ビルドするためには、android.jar の差し替え（ https://github.com/Reginer/aosp-android-jar ）と何故かプロジェクト内の `.idea` を消す必要があります。

## 実行方法
`Android Studio`が必要です

- このリポジトリをクローンします
  - `git clone https://github.com/takusan23/NewRadioSupporter`
  - git がない場合は zip をダウンロードして解凍しても良いかもしれないです
- Android Studio で開きます
  - ![Imgur](https://imgur.com/9n2ygdE.png)
- 暫く待つと、実行ボタンが押せるようになるので押します
  - ![Imgur](https://imgur.com/O5855id.png)

## そのほか
`*#*#4636#*#*`で`NR/LTE`にしておくと、SIMカードを抜いても動く？（なんで...）