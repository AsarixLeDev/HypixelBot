import base64
import io
import sys

import nbt


def decode_inventory_data(raw):
    data = nbt.nbt.NBTFile(fileobj=io.BytesIO(base64.b64decode(raw)))
    tagList = data.get("i")
    tag = tagList[0].get("tag")
    attr = tag.get("ExtraAttributes")
    print(attr.get("id"))


if __name__ == "__main__":
#    try:
    decode_inventory_data(sys.argv[1])
#    except Exception as e:
#        print(e)
