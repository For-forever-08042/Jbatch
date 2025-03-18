#!/usr/bin/expect
#-------------------------------------------------------------------------------
#    名称　　　　　：　メール送信
#    プログラムID　：　cmABmexpS
################################################################################
# ！！！要注意！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
# ！　本番環境ではGmailを利用してのメール送信のため、当該シェルは本番環境では ！
# ！　使用不可となります。                                                    ！
# ！　修正が必要な場合は、本番環境のシェルを直接編集してください。            ！
# ！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
################################################################################
#
#    【処理概要】
#       起動引数で渡された宛先に、メールを送信する。
#
#    【引数説明】
#       第１引数                :　SMTPサーバー
#       第２引数                :　クライアントドメイン
#       第３引数                :　メールの送り主アドレス
#       第４引数                :　メールの宛先アドレス
#       第５引数                :　メール件名
#       第６引数                :　メール本文の内容を格納しているテキストファイル
#       第７引数                :　メールの宛先アドレスCC
#
#    【戻り値】
#       10　　 ：　正常
#       49　　 ：　警告
#
#-------------------------------------------------------------------------------
#    稼働環境
#      Red Hat Enterprise Linux 6
#
#    改定履歴
#      1.00 :   2015/09/07 SSI.上野  ： 初版
#     40.00 :   2023/05/31 SSI.張シン  ： MCCM初版（CC追加）
#-------------------------------------------------------------------------------
#  $Id:$
#-------------------------------------------------------------------------------
#  Copyright (C) 2012 NTT DATA CORPORATION
#-------------------------------------------------------------------------------
set timeout 300

# 変数セット
set MAILSVR         [lindex $argv 0]
set MAILDOMAIN      [lindex $argv 1]
set FROM_ADDR       [lindex $argv 2]
set TO_ADDR         [lindex $argv 3]
set MAIL_SUBJECT    [lindex $argv 4]
set FILE_BODY       [lindex $argv 5]
set CC_ADDR         [lindex $argv 6]

# メール本文
set DATA_BODY [open ${FILE_BODY} r]

# telnetログイン
spawn telnet ${MAILSVR} 25
expect {
    #
    -re "220.*\n" {
        send "HELO ${MAILDOMAIN}\r"
        #exp_continue
    }

    # 想定外の場合はエラーとしてexit
    default {
        echo 41
        exit 41
    }
}

expect {
    #
    -re "250.*\n" {
        send "MAIL FROM: ${FROM_ADDR}\r"
        #exp_continue
    }

    # 想定外の場合はエラーとしてexit
    default {
        echo 42
        exit 42
    }
}

expect {
    #
    -re "250.*\n" {
        foreach TO_ADR [split ${TO_ADDR} ";"] {
            send "RCPT TO: ${TO_ADR}\r"
        }
        foreach CC_ADR [split ${CC_ADDR} ";"] {
            send "RCPT TO: ${CC_ADR}\r"
        }
        #exp_continue
    }

    # 想定外の場合はエラーとしてexit
    default {
        echo 43
        exit 43
    }
}

expect {
    #
    -re "250.*\n" {
        send "DATA\r"
        #exp_continue
    }

    # 想定外の場合はエラーとしてexit
    default {
        echo 44
        exit 44
    }
}

expect {
    #
    -re "354.*\n" {
        send "\'Content-Type: text/plain; charaset=\"iso-2022-jp\"\'\r"
        send "FROM: ${FROM_ADDR}\r"
        send "Subject:${MAIL_SUBJECT}\r"

        send "TO: ${TO_ADDR}\r"
        send "CC: ${CC_ADDR}\r\r"
        send "\r"
        while {![eof ${DATA_BODY}]} {
            gets  ${DATA_BODY} data
            send -- "${data}\r"
         }
        send ".\r"
        #exp_continue
    }

    # 想定外の場合はエラーとしてexit
    default {
        echo 45
        exit 45
    }
}

expect {
    #
    -re "250.*\n" {
        send "QUIT\r"
        #exp_continue
    }

    # 想定外の場合はエラーとしてexit
    default {
        echo 46
        exit 46
    }
}

echo 10
exit 10
