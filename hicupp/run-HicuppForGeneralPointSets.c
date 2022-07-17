// Launcher for the Linux release

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

char targetCmd[] = "bin/HicuppForGeneralPointSets";

int main(int argc, char **argv) {
//  printf("argv[0] == \"%s\"\n", argv[0]);
  int commandLength = strlen(argv[0]) + 50;
  char *command = malloc(commandLength);
  strcpy(command, argv[0]);
  char *lastSlash = strrchr(command, '/');
  if (lastSlash == NULL)
    strcpy(command, targetCmd);
  else
    strcpy(lastSlash + 1, targetCmd);
  return system(command);
}
