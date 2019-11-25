	Program Survivability
****** 
*      parameter (ng=2,nv=2,nh=2,nm=ng+nh+nv) ! 2g ring
*      parameter (ng=3,nv=3,nh=3,nm=ng+nh+nv) ! 3g ring
*      parameter (ng=2,nv=2,nh=8,nm=ng+nh+nv) ! 2g web
*      parameter (ng=3,nv=3,nh=12,nm=ng+nh+nv) ! 3g web
*      parameter (ng=2,nv=4,nh=10,nm=ng+nh+nv) ! 2g utility ini
*      parameter (ng=2,nv=4,nh=18,nm=ng+nh+nv) ! 2g utility 1,6
*      parameter (ng=2,nv=4,nh=15,nm=ng+nh+nv) ! 2g utility 7
*      parameter (ng=2,nv=4,nh=11,nm=ng+nh+nv) ! 2g utility 2(nh=16),3(nh=14), 
*                                              !         4(nh=13),5(nh=11)
      parameter (ng=4,nv=4,nh=8,nm=ng+nh+nv) ! 4g partitioned mesh 
*      parameter (ng=8,nv=13,nh=22,nm=ng+nh+nv) ! 8g utility 
	   integer(8) m,nt,in,Etot,Eact,l,nc,SNM
	   integer(8) X(nm),P(nm),E(nm),F(nm),S(nm),R(nm),N(nm)
	   integer(8) Y(nm,nm),Z(nm,nm),O(nm),C(nm)
      real(8)    PS,PR,PF,p1,p2,p3,pt 
****** nm is the total number of elements
****** ng is the number of generators
****** nv is the number of vertical links
****** nh is the number of horizontal links

****** we represent a generator bus as a graph X, and then 
****** each element is a graph element X(i). The elements X(1)...X(ng)
****** are generators, X(ng+1)...X(ng+nv) are vertical links, and  
****** X(ng+nv+1)...X(nm) are horizontal links.

****** Map Y(i,j) of the graph describes the connection between different 
****** elements: Y(i,j)=1 means that the elements X(i) and X(j) are 
****** connected, if Y(i,j)=0, it means that they are not
      Y=0
      call topology(nm,Y)

****** we assign to each element some quality E (here, power). 
****** If the i-element of the X-graph is a generator, E(i)=1;
****** E(i)=-1 if it is a vertical link, and E(i)=0 if it is a horizontal link

      do i=1,nm
	  E(i)=0
	  if(i.le.ng) E(i)=1
	  if(i.gt.ng.and.i.le.ng+nv) E(i)=-1
	  enddo

****** Each graph element can be in "on" (available) or "off" (unavailable) 
****** state. If the i-element of the X-graph is in "on"-state,  
****** we assign it P(i)=1, P(i)=0 if it is unavailable.

****** nt is the total number of combinations of available/unavailable elements.
****** m  is the number of damaged (unavailable) elements

****** Etot is the power produced initially by all available generators
****** Eact is the power available to the rest of IPS after damage 

      Etot=ng

****** F(m) is the number of combinations (scenarios) leading to Eact=0
****** S(m) is the number of combinations (scenarios) leading to Eact=Etot
****** R(m) is the number of combinations (scenarios) leading to Eact<Etot
****** N(m) is the total number of scenarios at a given m (m runs from 1 to nm)

      nt=2**nm
      F=0
	  S=0
	  R=0
      N=0
****** Step 1: generate a combination of available/unavailable elements	
      do 5 k=1,nt
      write(*,*) 'nt=',nt,'k=',k
      P=0
	  in=k-1
      
	  Eact=Etot
    
	  do i=1,nm
      l=in/2
      P(i)=in-2*l
      in=l
      enddo

****** Now we check how many elements are damaged in the combination
*      write(*,*) 'k=',k

      m=0
      do i=1,nm
      if(P(i).eq.0)m=m+1 
      enddo
      if(m.eq.0) goto 5
****** Z(i,j) reflects the connection between elements after damage
      Z=0
      do i=1,nm
	  do j=1,nm
      Z(i,j)=Y(i,j)*P(i)*P(j)
	  enddo
	  enddo

****** Step 2: we check how much power is available for the rest of IPS    
****** that is, how many generators are available and connected to
****** vertical links
      do 1 i=1,ng
	  if(P(i).eq.0) then
	  Eact=Eact-E(i)
	  goto 1
      endif

****** Here we check whether an available generator (X(1)..X(ng)) 
****** is connected to a vertical link (X(ng+1)..X(ng+nv))
      nc=0
      C=0
      O=0
******
	  j1=1
      C(j1)=i
	  k1=i
      j2=0

  3   continue

	  do 2 k2=1,nm
      do jj1=1,nm
      if(k2.eq.C(jj1).or.k2.eq.O(jj1)) goto 2
	  enddo
 
	  if(Z(k1,k2).eq.1)then

	  if(k1.ge.ng+1.and.k1.le.ng+nv)then
      nc=1
	  goto 1 
	  endif
	  if(k2.ge.ng+1.and.k2.le.ng+nv)then
      nc=1
	  goto 1 
	  endif

      j2=j2+1
      O(j2)=k2
      endif

  2	  continue
      
      if(j2.gt.0)then
	  k1=O(j2)
      j1=j1+1
      C(j1)=O(j2)
	  O(j2)=0
      j2=j2-1
      goto 3
	  endif

****** If there is no connection, then Eact is decreased
      if(nc.eq.0)	Eact=Eact-E(i)
  1   continue

****** The end of Step 2

	  if(Eact.eq.Etot) S(m)=S(m)+1
	  if(Eact.eq.0) F(m)=F(m)+1
	  if(Eact.gt.0.and.Eact.lt.Etot) R(m)=R(m)+1
	
	  N(m)=N(m)+1
****** The end of Step 1, generate the next combination of available/unavailable elements
****** or quit the program
  5   continue

      SNM=1
      do m=1,nm
      if(S(m)+R(m)+F(m).ne.N(m)) then
      write(*,*) 'ERROR at m=',m
	  endif
      SNM=N(m)+SNM
	  enddo
      if(SNM.ne.nt) then
      write(*,*) 'ERROR'
	  endif

      open(14,File='Scenarios.dat')
      write(14,*) 'm:S,R,F,N'

	  open(15,File='Probabilities.dat')
      write(15,*) 'm:P(S),P(R),P(F)'

      do m=1,nm
      p1=S(m)
	  p2=R(m)
	  p3=F(m)
	  pt=N(m)
	  PS=p1/pt
	  PR=p2/pt
	  PF=p3/pt
      write(14,92) m,S(m),R(m),F(m),N(m)
	  write(15,93) m,PS,PR,PF
      enddo

	  close(14)
	  close(15)

  92  format(i4,4i20)		    
  93  format(i4,3f10.3)

      stop
	  end
**************************************
*     This is collection of topologies
**************************************
      subroutine topology(nm,Y)
	  integer(8) Y(nm,nm)
****** This topology represents a ring with two generators
****** generators are nodes 1 and 2 and vertical links are nodes 3 and 4
*	Y(1,3)=1
*	Y(1,5)=1
*      Y(1,6)=1 
*	Y(2,4)=1
*	Y(2,5)=1
*      Y(2,6)=1 
*	Y(3,1)=1
*	Y(5,1)=1
*      Y(6,1)=1 
*	Y(4,2)=1
*	Y(5,2)=1
*      Y(6,2)=1 

****** This topology represents a ring with three generators
****** generators are nodes 1, 2 and 3  
****** vertical links are nodes 4, 5 and 6
*	Y(1,4)=1
*	Y(1,7)=1
*      Y(1,9)=1 
*	Y(2,5)=1
*	Y(2,7)=1
*      Y(2,8)=1 
*	Y(3,6)=1
*	Y(3,8)=1
*      Y(3,9)=1 
*	Y(4,1)=1
*	Y(5,2)=1
*      Y(6,2)=1 
*      Y(6,3)=1 
*	Y(7,1)=1
*	Y(7,2)=1
*	Y(8,2)=1
*      Y(8,3)=1 
*	Y(9,1)=1
*      Y(9,3)=1 

****** This topology represents a web with two generators
****** generators are nodes 1, 2   
****** vertical links are nodes 3, 4 
*      Y(1,3)=1
*      Y(1,5)=1
*      Y(1,6)=1
*      Y(1,7)=1
*      Y(2,4)=1
*      Y(2,8)=1
*      Y(2,9)=1
*      Y(2,10)=1
*      Y(3,1)=1
*      Y(4,2)=1
*      Y(5,1)=1
*      Y(5,8)=1
*      Y(5,11)=1
*      Y(6,1)=1
*      Y(6,9)=1
*      Y(6,11)=1
*      Y(6,12)=1
*      Y(7,1)=1
*      Y(7,10)=1
*      Y(7,12)=1
*      Y(8,2)=1
*      Y(8,5)=1
*      Y(8,11)=1
*      Y(9,2)=1
*      Y(9,6)=1
*      Y(9,11)=1
*      Y(9,12)=1
*      Y(10,2)=1
*      Y(10,7)=1
*      Y(10,12)=1
*      Y(11,5)=1
*      Y(11,6)=1
*      Y(11,8)=1
*      Y(11,9)=1
*      Y(11,12)=1
*      Y(12,6)=1
*      Y(12,7)=1
*      Y(12,9)=1
*      Y(12,10)=1
*      Y(12,11)=1

****** This topology represents a web with three generators
****** generators are nodes 1, 2, and 3   
****** vertical links are nodes 4, 5, and 6 

*      Y(1,4)=1
*	Y(1,7)=1
*	Y(1,8)=1
*	Y(1,9)=1
*      Y(2,5)=1
*	Y(2,10)=1
*	Y(2,11)=1
*	Y(2,12)=1
*      Y(3,6)=1
*	Y(3,13)=1
*	Y(3,14)=1
*	Y(3,15)=1
*      Y(4,1)=1
*      Y(5,2)=1
*      Y(6,3)=1
*      Y(7,1)=1
*      Y(7,13)=1
*      Y(7,18)=1
*      Y(8,1)=1
*      Y(8,11)=1
*      Y(8,14)=1
*      Y(8,16)=1
*      Y(8,17)=1
*      Y(8,18)=1
*      Y(9,1)=1
*      Y(9,12)=1
*      Y(9,16)=1
*      Y(10,2)=1
*      Y(10,15)=1
*      Y(10,17)=1
*      Y(11,2)=1
*      Y(11,8)=1
*      Y(11,14)=1
*      Y(11,16)=1
*      Y(11,17)=1
*      Y(11,18)=1
*      Y(12,2)=1
*      Y(12,9)=1
*      Y(12,16)=1
*      Y(13,3)=1
*      Y(13,7)=1
*      Y(13,18)=1
*      Y(14,3)=1
*      Y(14,8)=1
*      Y(14,11)=1
*      Y(14,16)=1
*      Y(14,17)=1
*      Y(14,18)=1
*      Y(15,3)=1
*      Y(15,10)=1
*      Y(15,17)=1
*      Y(16,8)=1
*      Y(16,9)=1
*      Y(16,11)=1
*      Y(16,12)=1
*      Y(16,14)=1
*      Y(16,17)=1
*      Y(16,18)=1
*      Y(17,8)=1
*      Y(17,10)=1
*      Y(17,11)=1
*      Y(17,14)=1
*      Y(17,15)=1
*      Y(17,16)=1
*      Y(17,18)=1
*      Y(18,7)=1
*      Y(18,8)=1
*      Y(18,13)=1
*      Y(18,14)=1
*      Y(18,11)=1
*      Y(18,16)=1
*      Y(18,17)=1

****** This topology represents a real power grid with two generators (ini)
****** generators are nodes 1, 2   
****** vertical links are nodes 3, 4, 5, and 6 
*	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,10)=1
*      Y(3,11)=1
*      Y(3,12)=1
*      Y(3,16)=1
*      Y(4,11)=1
*      Y(4,13)=1
*      Y(4,14)=1
*      Y(5,12)=1
*      Y(5,14)=1
*      Y(5,15)=1
*      Y(6,15)=1
*      Y(6,16)=1
*      Y(7,9)=1
*      Y(7,10)=1
*      Y(7,11)=1
*      Y(7,12)=1
*      Y(7,16)=1
*      Y(8,9)=1
*      Y(8,10)=1
*      Y(8,13)=1
*      Y(9,10)=1
*      Y(9,11)=1
*      Y(9,12)=1
*      Y(9,13)=1
*      Y(9,16)=1
*      Y(10,11)=1
*      Y(10,12)=1
*      Y(10,13)=1
*      Y(10,16)=1
*      Y(11,12)=1
*      Y(11,13)=1
*      Y(11,14)=1
*      Y(11,16)=1
*      Y(12,14)=1
*      Y(12,15)=1
*      Y(12,16)=1
*      Y(13,14)=1
*      Y(14,15)=1
*      Y(15,16)=1
**************** 2g(1, nh=18)
*	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,11)=1
*      Y(3,23)=1
*      Y(4,14)=1
*      Y(4,15)=1
*      Y(4,16)=1
*      Y(5,15)=1
*      Y(5,17)=1
*      Y(5,18)=1
*      Y(6,19)=1
*      Y(6,21)=1
*      Y(6,22)=1
*      Y(7,9)=1
*      Y(7,11)=1
*      Y(7,23)=1
*      Y(8,10)=1
*      Y(8,13)=1
*      Y(8,14)=1
*      Y(9,10)=1
*      Y(9,11)=1
*      Y(9,12)=1
*      Y(9,23)=1
*      Y(10,12)=1
*      Y(10,13)=1
*      Y(10,14)=1
*      Y(11,12)=1
*      Y(11,13)=1
*      Y(11,16)=1
*      Y(11,17)=1
*      Y(11,20)=1
*      Y(11,21)=1
*      Y(11,23)=1
*      Y(11,24)=1
*      Y(12,13)=1
*      Y(12,16)=1
*      Y(12,17)=1
*      Y(12,20)=1
*      Y(12,21)=1
*      Y(12,24)=1
*      Y(13,14)=1
*      Y(13,16)=1
*      Y(13,17)=1
*      Y(13,20)=1
*      Y(13,21)=1
*      Y(13,24)=1
*      Y(14,15)=1
*      Y(14,16)=1
*      Y(15,16)=1
*      Y(15,17)=1
*      Y(15,18)=1
*      Y(16,17)=1
*      Y(16,20)=1
*      Y(16,21)=1
*      Y(16,24)=1
*      Y(17,18)=1
*      Y(17,20)=1
*      Y(17,21)=1
*      Y(17,24)=1
*      Y(18,19)=1
*      Y(18,20)=1
*      Y(19,20)=1
*      Y(19,21)=1
*      Y(19,22)=1
*      Y(20,21)=1
*      Y(20,24)=1
*      Y(21,22)=1
*      Y(21,24)=1
*      Y(22,23)=1
*      Y(22,24)=1
*      Y(23,24)=1
**************** 2g (2,links 23,24 are out)
*	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,11)=1
*      Y(3,22)=1
*      Y(4,14)=1
*      Y(4,15)=1
*      Y(4,16)=1
*      Y(5,15)=1
*      Y(5,17)=1
*      Y(5,18)=1
*      Y(6,19)=1
*      Y(6,21)=1
*      Y(6,22)=1
*      Y(7,9)=1
*      Y(7,11)=1
*      Y(7,22)=1
*      Y(8,10)=1
*      Y(8,13)=1
*      Y(8,14)=1
*      Y(9,10)=1
*      Y(9,11)=1
*      Y(9,12)=1
*      Y(9,22)=1
*      Y(10,12)=1
*      Y(10,13)=1
*      Y(10,14)=1
*      Y(11,12)=1
*      Y(11,13)=1
*      Y(11,16)=1
*      Y(11,17)=1
*      Y(11,20)=1
*      Y(11,21)=1
*      Y(11,22)=1
*      Y(12,13)=1
*      Y(12,16)=1
*      Y(12,17)=1
*      Y(12,20)=1
*      Y(12,21)=1
*      Y(13,14)=1
*      Y(13,16)=1
*      Y(13,17)=1
*      Y(13,20)=1
*      Y(13,21)=1
*      Y(14,15)=1
*      Y(14,16)=1
*      Y(15,16)=1
*      Y(15,17)=1
*      Y(15,18)=1
*      Y(16,17)=1
*      Y(16,20)=1
*      Y(16,21)=1
*      Y(17,18)=1
*      Y(17,20)=1
*      Y(17,21)=1
*      Y(18,19)=1
*      Y(18,20)=1
*      Y(19,20)=1
*      Y(19,21)=1
*      Y(19,22)=1
*      Y(20,21)=1
*      Y(21,22)=1
**************** 2g (3,links 19,20,23,24 are out)      
*	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,11)=1
*      Y(3,20)=1
*      Y(4,14)=1
*      Y(4,15)=1
*      Y(4,16)=1
*      Y(5,15)=1
*      Y(5,17)=1
*      Y(5,18)=1
*      Y(6,18)=1
*      Y(6,19)=1
*      Y(6,20)=1
*      Y(7,9)=1
*      Y(7,11)=1
*      Y(7,20)=1
*      Y(8,10)=1
*      Y(8,13)=1
*      Y(8,14)=1
*      Y(9,10)=1
*      Y(9,11)=1
*      Y(9,12)=1
*      Y(9,20)=1
*      Y(10,12)=1
*      Y(10,13)=1
*      Y(10,14)=1
*      Y(11,12)=1
*      Y(11,13)=1
*      Y(11,16)=1
*      Y(11,17)=1
*      Y(11,19)=1
*      Y(11,20)=1
*      Y(12,13)=1
*      Y(12,16)=1
*      Y(12,17)=1
*      Y(12,19)=1
*      Y(13,14)=1
*      Y(13,16)=1
*      Y(13,17)=1
*      Y(13,19)=1
*      Y(14,15)=1
*      Y(14,16)=1
*      Y(15,16)=1
*      Y(15,17)=1
*      Y(15,18)=1
*      Y(16,17)=1
*      Y(16,19)=1
*      Y(17,18)=1
*      Y(17,19)=1
*      Y(18,19)=1
*      Y(18,20)=1
*      Y(19,20)=1
**************** 2g (4,links 19,20,21,23,24 are out)      
*	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,11)=1
*      Y(3,19)=1
*      Y(4,14)=1
*      Y(4,15)=1
*      Y(4,16)=1
*      Y(5,15)=1
*      Y(5,17)=1
*      Y(5,18)=1
*      Y(6,18)=1
*      Y(6,19)=1
*      Y(7,9)=1
*      Y(7,11)=1
*      Y(7,19)=1
*      Y(8,10)=1
*      Y(8,13)=1
*      Y(8,14)=1
*      Y(9,10)=1
*      Y(9,11)=1
*      Y(9,12)=1
*      Y(9,19)=1
*      Y(10,12)=1
*      Y(10,13)=1
*      Y(10,14)=1
*      Y(11,12)=1
*      Y(11,13)=1
*      Y(11,16)=1
*      Y(11,17)=1
*      Y(11,19)=1
*      Y(12,13)=1
*      Y(12,16)=1
*      Y(12,17)=1
*      Y(13,14)=1
*      Y(13,16)=1
*      Y(13,17)=1
*      Y(14,15)=1
*      Y(14,16)=1
*      Y(15,16)=1
*      Y(15,17)=1
*      Y(15,18)=1
*      Y(16,17)=1
*      Y(17,18)=1
*      Y(18,19)=1
**************** 2g (5,links 10,12,19,20,21,23,24 are out)      
*	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,10)=1
*      Y(3,17)=1
*      Y(4,12)=1
*      Y(4,13)=1
*      Y(4,14)=1
*      Y(5,13)=1
*      Y(5,15)=1
*      Y(5,16)=1
*      Y(6,16)=1
*      Y(6,17)=1
*      Y(7,9)=1
*      Y(7,10)=1
*      Y(7,17)=1
*      Y(8,9)=1
*      Y(8,11)=1
*      Y(8,12)=1
*      Y(9,10)=1
*      Y(9,11)=1
*      Y(9,12)=1
*      Y(9,17)=1
*      Y(10,11)=1
*      Y(10,14)=1
*      Y(10,15)=1
*      Y(10,17)=1
*      Y(11,12)=1
*      Y(11,14)=1
*      Y(11,15)=1
*      Y(12,13)=1
*      Y(12,14)=1
*      Y(13,14)=1
*      Y(13,15)=1
*      Y(13,16)=1
*      Y(14,15)=1
*      Y(15,16)=1
*      Y(16,17)=1
**************** 2g(6, nh=18)
* 	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,16)=1
*      Y(4,11)=1
*      Y(4,12)=1
*      Y(4,17)=1
*      Y(4,18)=1
*      Y(4,21)=1
*      Y(5,12)=1
*      Y(5,13)=1
*      Y(6,14)=1
*      Y(6,15)=1
*      Y(7,9)=1
*      Y(7,16)=1
*      Y(8,10)=1
*      Y(8,11)=1
*      Y(9,10)=1
*      Y(9,16)=1
*      Y(9,17)=1
*      Y(9,20)=1
*      Y(9,23)=1
*      Y(10,11)=1
*      Y(10,17)=1
*      Y(10,20)=1
*      Y(10,23)=1
*      Y(11,12)=1
*      Y(11,17)=1
*      Y(11,18)=1
*      Y(11,21)=1
*      Y(12,13)=1
*      Y(12,17)=1
*      Y(12,18)=1
*      Y(12,21)=1
*      Y(13,14)=1
*      Y(13,18)=1
*      Y(13,19)=1
*      Y(13,24)=1
*      Y(14,15)=1
*      Y(14,18)=1
*      Y(14,19)=1
*      Y(14,24)=1
*      Y(15,16)=1
*      Y(15,19)=1
*      Y(15,20)=1
*      Y(15,22)=1
*      Y(16,19)=1
*      Y(16,20)=1
*      Y(16,22)=1
*      Y(17,18)=1
*      Y(17,20)=1
*      Y(17,21)=1
*      Y(17,23)=1
*      Y(18,19)=1
*      Y(18,21)=1
*      Y(18,24)=1
*      Y(19,20)=1
*      Y(19,22)=1
*      Y(19,24)=1
*      Y(20,22)=1
*      Y(20,23)=1
*      Y(21,22)=1
*      Y(21,23)=1
*      Y(21,24)=1
*      Y(22,23)=1
*      Y(22,24)=1
*      Y(23,24)=1
**************** 2g(7, nh=15)
* 	Y(1,7)=1
*      Y(2,8)=1
*      Y(3,7)=1
*      Y(3,9)=1
*      Y(3,16)=1
*      Y(4,11)=1
*      Y(4,12)=1
*      Y(4,17)=1
*      Y(4,18)=1
*      Y(4,21)=1
*      Y(5,12)=1
*      Y(5,13)=1
*      Y(6,14)=1
*      Y(6,15)=1
*      Y(7,9)=1
*      Y(7,16)=1
*      Y(8,10)=1
*      Y(8,11)=1
*      Y(9,10)=1
*      Y(9,16)=1
*      Y(9,17)=1
*      Y(9,20)=1
*      Y(10,11)=1
*      Y(10,17)=1
*      Y(10,20)=1
*      Y(11,12)=1
*      Y(11,17)=1
*      Y(11,18)=1
*      Y(11,21)=1
*      Y(12,13)=1
*      Y(12,17)=1
*      Y(12,18)=1
*      Y(12,21)=1
*      Y(13,14)=1
*      Y(13,18)=1
*      Y(13,19)=1
*      Y(14,15)=1
*      Y(14,18)=1
*      Y(14,19)=1
*      Y(15,16)=1
*      Y(15,19)=1
*      Y(15,20)=1
*      Y(15,21)=1
*      Y(16,19)=1
*      Y(16,20)=1
*      Y(16,21)=1
*      Y(17,18)=1
*      Y(17,20)=1
*      Y(17,21)=1
*      Y(18,19)=1
*      Y(18,21)=1
*      Y(19,20)=1
*      Y(19,21)=1
*      Y(20,21)=1
*      Y(21,21)=1
**************** 8g
*      Y(1,9)=1
*      Y(1,22)=1
*	Y(1,30)=1
*      Y(2,11)=1
*      Y(2,23)=1
*	Y(2,24)=1
*	Y(2,35)=1
*      Y(3,13)=1
*      Y(3,25)=1
*	Y(3,26)=1
*	Y(3,34)=1
*      Y(4,14)=1
*	Y(4,26)=1
*      Y(4,27)=1
*	Y(4,35)=1
*	Y(4,36)=1
*      Y(5,16)=1
*      Y(5,28)=1
*	Y(5,29)=1
*      Y(6,18)=1
*      Y(6,36)=1
*	Y(6,37)=1
*      Y(6,39)=1
*	Y(6,40)=1
*	Y(6,43)=1
*      Y(7,19)=1
*	Y(7,40)=1
*      Y(7,41)=1
*      Y(8,21)=1
*      Y(8,37)=1
*	Y(8,38)=1
*      Y(8,42)=1
*      Y(10,22)=1
*	Y(10,23)=1
*	Y(10,31)=1
*	Y(10,32)=1
*	Y(10,34)=1
*      Y(12,24)=1
*	Y(12,25)=1
*      Y(15,27)=1
*	Y(15,28)=1
*	Y(15,32)=1
*	Y(15,33)=1
*	Y(15,38)=1
*      Y(15,39)=1
*      Y(17,29)=1
*	Y(17,30)=1
*	Y(17,31)=1
*	Y(17,33)=1
*      Y(20,41)=1
*	Y(20,42)=1
*	Y(20,43)=1
*      Y(22,23)=1
*      Y(22,31)=1
*	Y(22,32)=1
*	Y(22,34)=1
*	Y(23,31)=1
*      Y(23,32)=1
*	Y(23,34)=1
*      Y(24,25)=1
*      Y(27,28)=1
*	Y(27,32)=1
*      Y(27,33)=1
*      Y(27,38)=1
*	Y(27,39)=1
*      Y(28,32)=1
*	Y(28,33)=1
*      Y(28,38)=1
*	Y(28,39)=1
*      Y(29,30)=1
*	Y(29,31)=1
*      Y(29,33)=1
*	Y(30,31)=1
*      Y(30,33)=1
*      Y(31,32)=1
*	Y(31,33)=1
*      Y(31,34)=1
*	Y(32,33)=1
*      Y(32,34)=1
*	Y(32,38)=1
*      Y(32,39)=1
*	Y(33,38)=1
*      Y(33,39)=1
*	Y(38,39)=1
*	Y(41,42)=1
*      Y(41,43)=1
*      Y(42,43)=1
*
****** This topology represents a partitioned mesh with four generators
****** generators are nodes 1-4 and vertical links are nodes 5-8
	Y(1,5)=1
	Y(5,1)=1
	Y(1,9)=1
	Y(9,1)=1
      Y(1,12)=1 
      Y(1,13)=1 
      Y(12,1)=1 
      Y(13,1)=1 
	Y(2,6)=1
	Y(6,2)=1
	Y(2,9)=1
	Y(9,2)=1
      Y(2,10)=1 
	Y(10,2)=1
      Y(2,14)=1 
	Y(14,2)=1
	Y(3,7)=1
	Y(7,3)=1
	Y(3,10)=1
	Y(10,3)=1
      Y(3,11)=1 
	Y(11,3)=1
      Y(3,15)=1 
	Y(15,3)=1
	Y(4,8)=1
	Y(8,4)=1
	Y(4,11)=1
	Y(11,4)=1
      Y(4,12)=1 
	Y(12,4)=1
      Y(4,16)=1 
	Y(16,4)=1
      Y(13,16)=1 
	Y(16,13)=1
      Y(13,15)=1 
	Y(15,13)=1
      Y(13,14)=1 
	Y(14,13)=1
      Y(14,16)=1 
	Y(16,14)=1
      Y(14,15)=1 
	Y(15,14)=1
      Y(15,16)=1 
	Y(16,15)=1

	do j=1,nm
	do i=j+1,nm
      Y(i,j)=Y(j,i)
	enddo
	enddo

	return
	end
