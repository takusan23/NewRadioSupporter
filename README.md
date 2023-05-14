# NewRadioSupporter

アンテナピクト📶の隣に居座る5Gアイコンが何を示しているのか、その真実に迫るアプリです。  

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
- バックグラウンドでも通知領域から確認
- おまけ程度のバンド表示
    - `n258`とか

(なんか従量制ネットワークか無制限ネットワークか検出出来るよ！って書いてあったんだけど検出できてない。流石にできないやろ...)

# 開発者向け
JetpackComposeでできてます。最新のAndroid Studioで実行できるはずです。

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