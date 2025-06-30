import json
import os
import requests

apiAddress = "http://127.0.0.1:8081/"
urlPrefix = apiAddress + "bot" + os.getenv("TELEGRAM_TOKEN")


def findString(sourceStr, targetStr):
    if str(sourceStr).find(str(targetStr)) == -1:
        return False
    else:
        return True


def genFileDirectory(path):
    files_walk = os.walk(path)
    target = {
    }
    for root, dirs, file_name_dic in files_walk:
        for fileName in file_name_dic:
            if findString(fileName, "v8a"):
                target["arm64"] = (fileName, open(path + "/" + fileName, "rb"))
            if findString(fileName, "v7a"):
                target["armeabi"] = (fileName, open(
                    path + "/" + fileName, "rb"))
            if findString(fileName, "x86.apk"):
                target["i386"] = (fileName, open(path + "/" + fileName, "rb"))
            if findString(fileName, "x86_64"):
                target["amd64"] = (fileName, open(path + "/" + fileName, "rb"))

    return target


def sendDocument(user_id, path, message="", entities=None):
    files = {'document': open(path, 'rb')}
    data = {'chat_id': user_id,
            'caption': message,
            'parse_mode': 'Markdown',
            'caption_entities': entities}
    response = requests.post(
        urlPrefix + "/sendDocument", files=files, data=data)
    print(response.json())


def sendAPKs(path):
    # --- 新增: 读取 changelog 文件内容 ---
    changelog_content = ""
    try:
        # 假设 changelog.txt 被下载到了工作目录的根路径
        with open('changelog.txt', 'r', encoding='utf-8') as f:
            changelog_content = f.read().strip()
    except FileNotFoundError:
        print("Warning: changelog.txt not found. Sending message without changelog.")
        changelog_content = "Changelog not available."

    header = "#app #apk #YetAnotherCalendarWidget"
    repo_url = "https://github.com/Steve-Mr/YetAnotherCalendarWidget"

    # 将各部分组合成最终消息
    # 使用 f-string 方便地组合字符串
    final_message = f"""{header}
**Release Notes:**
```
{changelog_content}
```
{repo_url}
"""
    apks = os.listdir(path)
    if not apks:
        print("Error: No APKs found in the 'apks' directory.")
        return

    apks.sort()
    apk_to_send = os.path.join(path, apks[0])  # 只发送第一个APK

    print(f"Sending APK: {apk_to_send}")
    print("--- Message Content ---")
    print(final_message)
    print("-----------------------")

    sendDocument(
        user_id="@maaryIsTyping",
        path=apk_to_send,
        message=final_message
    )


if __name__ == '__main__':
    sendAPKs("./apks")
