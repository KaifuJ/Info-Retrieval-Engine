# calculate possibilities for each word in the dictionary

def calPos():
    with open("cs221_frequency_dictionary_en.txt", "r") as file:
        fList = file.readlines()

    sum = 0

    for i in range(len(fList)):
        fList[i] = fList[i].split()
        sum += int(fList[i][1])

    for l in fList:
        l.append(int(l[1]) / sum)

    print(len(fList))

    with open("cs221_frequency_dictionary_en.txt", "w") as file:
        for list in fList:
            file.write(list[0] + " ")
            file.write(list[1] + " ")
            file.write(str(list[2]) + "\n")

calPos()
