import csv
import os

def intTryParse(value):
    try:
        return int(value), True
    except ValueError:
        return value, False



inputfile = csv.reader(open('london-restaurant.csv','r'))
outputfile = csv.writer(open('london-restaurant-clean.csv','w+'))

first = True
#
# for row in inputfile:
#     if first:
#         first = False
#         outputfile.writerow(row)
#         continue
#
#     if row[1] == "poi":
#         outputfile.writerow(row)

for row in inputfile:
    if first:
        first = False
        outputfile.writerow(row)
        continue

    value, res = intTryParse(row[0])
    if res == True:
        outputfile.writerow(row)





